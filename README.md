# Machine Learning in Android 11

![GitHub followers](https://img.shields.io/github/followers/Rishit-dagli?label=Follow&style=social)
![Twitter Follow](https://img.shields.io/twitter/follow/rishit_dagli?style=social)
![Twitter URL](https://img.shields.io/twitter/url?style=social&url=https%3A%2F%2Fgithub.com%2FRishit-dagli%2FAndroid-Stream-Day-2020)

|![Build ML-Example-App](https://github.com/Rishit-dagli/Android-Stream-Day-2020/workflows/Build%20ML-Example-App/badge.svg)|![Build MLKitCustom](https://github.com/Rishit-dagli/Android-Stream-Day-2020/workflows/Build%20MLKitCustom/badge.svg)|
|---|---|

This repository demonstrates how you can get started with on-device ML with 
tools or plugins specifically launched with 
[Android 11](https://developer.android.com/11). If you have earlier worked with 
ML in Android, you will explore easier ways to integrate your ML applications 
with your Android apps. In this repository I majorly demonstrate the two 
biggest updates: 
[ML Model Binding Plugin](https://android-developers.googleblog.com/2020/06/tools-for-custom-ML-models.html)
and the [new ML Kit](https://android-developers.googleblog.com/2020/06/tools-for-custom-ML-models.html)
.

You can know more or watch the talks I gave on this topic - 
[talks.md](https://github.com/Rishit-dagli/Android-Stream-Day-2020/blob/master/talks.md)

## Why care about on-device ML in Android?

As you might have noticed we majorly focus on on-device ML here, Android 11 has 
a lot of cool updates for on-device ML but let's talk in brief about why you 
should care about it, you will also understand why there is such a hype about 
on-device ML or ML on edge.

### The idea behind on-device ML

While performing on-device ML opposed to the traditional approach you no longer 
send data to a server or some cloud based system which does the ML part for you 
and then returns me the outputs. So as an example if you were classifying if an 
image as an example if the image contains cat or dog, you would no longer send 
the data here the image to a server. You would instead do the inference on the 
data on the device itself, do all the computation on the device itself.

![](images/on-evice-ml-idea.jpg)

You would not directly use the model for your edge device. You would need to 
compress it or optimize the model so you can run it on the edge device as it 
has limited computation power, network availaibility and disk space. In this
document however, we will not be discussing about the optimization proccess. We 
will be deploying a `.tflite` model file. You can read more about 
[TensorFlow Lite](https://www.tensorflow.org/lite/) and the 
[Model Optimization process](https://www.tensorflow.org/lite/performance/model_optimization)
with TensorFlow Lite.

### Advantages of on-device ML

Here I have listed some advantages of using on-device ML:

- Power consumption

So the first thing that would come to your mind is power consumption, you spend 
a lot of power sending or streaming video data continuously to a server and 
sometimes it becomes infeasible to do so. However, also worth a mention 
sometimes the opposite could also be true when you employ heavy pre-processing.

- Inference time

Another important thing to consider is the time it takes me get the output or
essentially run the model. For real time applications this is a pretty important
aspect to consider. Without sending the data and the having to receive it back 
I speed up my inference time too.

- Network availability 

Using the traditional approach is also expensive in terms of network 
availability. I should have the bandwidth or network to continuously send the 
data and receive inferences from the server. 

- Security 

And finally security I no longer send data to a server or cloud based system, 
I no longer send data out of the device at all thus enforcing security.
