package com.amplifyframework.ui.liveness

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.amplifyframework.auth.AWSCredentials
import com.amplifyframework.auth.AWSCredentialsProvider
import com.amplifyframework.core.Action
import com.amplifyframework.core.Consumer
import com.amplifyframework.ui.liveness.model.FaceLivenessDetectionException
import com.amplifyframework.ui.liveness.ui.AlwaysOnMaxBrightnessScreen
import com.amplifyframework.ui.liveness.ui.ChallengeOptions
import com.amplifyframework.ui.liveness.ui.ChallengeView
import com.amplifyframework.ui.liveness.ui.DetectorStateKey
import com.amplifyframework.ui.liveness.ui.LockPortraitOrientation
import com.amplifyframework.ui.liveness.util.hasCameraPermission
import kotlinx.coroutines.launch

/**
 * @param sessionId of challenge
 * @param region AWS region to stream the video to. Current supported regions are listed in [add link here]
 * @param credentialsProvider to provide custom CredentialsProvider for authentication. Default uses initialized Amplify.Auth CredentialsProvider
 * @param disableStartView to bypass warmup screen.
 * @param onComplete callback notifying a completed challenge
 * @param onError callback containing exception for cause
 */
@Composable
fun TadaFaceLivenessDetector(
    sessionId: String,
    region: String,
    credentialsProvider: AWSCredentialsProvider<AWSCredentials>? = null,
    disableStartView: Boolean = false,
    onComplete: Action,
    onError: Consumer<FaceLivenessDetectionException>
) = TadaFaceLivenessDetector(
    sessionId,
    region,
    credentialsProvider,
    disableStartView,
    onComplete,
    onError,
    ChallengeOptions()
)

/**
 * @param sessionId of challenge
 * @param region AWS region to stream the video to. Current supported regions are listed in [add link here]
 * @param credentialsProvider to provide custom CredentialsProvider for authentication. Default uses initialized Amplify.Auth CredentialsProvider
 * @param disableStartView to bypass warmup screen.
 * @param challengeOptions is the list of ChallengeOptions that are to be overridden from the default configuration
 * @param onComplete callback notifying a completed challenge
 * @param onError callback containing exception for cause
 */
@Composable
fun TadaFaceLivenessDetector(
    sessionId: String,
    region: String,
    credentialsProvider: AWSCredentialsProvider<AWSCredentials>? = null,
    disableStartView: Boolean = false,
    onComplete: Action,
    onError: Consumer<FaceLivenessDetectionException>,
    challengeOptions: ChallengeOptions = ChallengeOptions(),
) {
    val scope = rememberCoroutineScope()
    val key = DetectorStateKey(sessionId, region, credentialsProvider)
    var isFinished by remember(key) { mutableStateOf(false) }
    val currentOnComplete by rememberUpdatedState(onComplete)
    val currentOnError by rememberUpdatedState(onError)

    if (isFinished) {
        return
    }

    // fails challenge if no camera permission set
    if (!LocalContext.current.hasCameraPermission()) {
        LaunchedEffect(key) {
            isFinished = true
            currentOnError.accept(FaceLivenessDetectionException.CameraPermissionDeniedException())
        }
        return
    }

    // fails challenge if session ID is empty
    if (sessionId.isBlank()) {
        LaunchedEffect(key) {
            isFinished = true
            currentOnError.accept(
                FaceLivenessDetectionException.SessionNotFoundException("Session ID cannot be empty.")
            )
        }
        return
    }

    // Locks portrait orientation for duration of challenge and resets on complete
    LockPortraitOrientation { resetOrientation ->
        Surface(color = MaterialTheme.colorScheme.background) {
            AlwaysOnMaxBrightnessScreen()
            ChallengeView(
                key = key,
                sessionId = sessionId,
                region,
                credentialsProvider = credentialsProvider,
                disableStartView,
                challengeOptions = challengeOptions,
                onChallengeComplete = {
                    scope.launch {
                        // if we are already finished, we already provided a result in complete or failed
                        if (!isFinished) {
                            isFinished = true
                            resetOrientation()
                            currentOnComplete.call()
                        }
                    }
                },
                onChallengeFailed = {
                    scope.launch {
                        // if we are already finished, we already provided a result in complete or failed
                        if (!isFinished) {
                            isFinished = true
                            resetOrientation()
                            currentOnError.accept(it)
                        }
                    }
                }
            )
        }
    }
}