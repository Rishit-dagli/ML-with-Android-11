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

## ML Model Binding Plugin

### What is the Model Binding Plugin about?

> Note: You need Android Studio 4.1 or aabove to be able to use 
> the Model Binding Plugin

You can make a fair enough guess from the name “Model Building” so as to what 
the [ML Model Binding Plugin](https://developer.android.com/studio/preview/features#tensor-flow-lite-models)
would do allow us to use custom TF Lite Models 
very easily. This lets developers import any TFLite model, read the input / 
output signature of the model, and use it with just a few lines of code that 
calls the open source TensorFlow Lite Android Support Library.

The ML model binding plugin makes it super easy for you to use a TF model in 
your app. You essentially have a lot less code to write that calls the 
TensorFlow Lite Android Support Library. If you have worked with TensorFlow 
Lite models you maybe know that you first need to convert everything to a 
`ByteArray` you no longer have to convert everything to `ByteArray` anymore with
ML Model Binding Plugin.

What I also love about this new plugin is you can easily use make use of GPUs 
and the NN API very easily. With the model binding plugin using them has never 
been easier. Using them is now just a dependency call and a single line of code
away, isn’t that cool what you can do with Model Binding plugin. With Android 11
The Neural Network API you also have unsigned integer weight support and a new 
Quality of Service (QOS) API too supporting even more edge scenarios. And Of 
course this would make your development a lot more faster with the features we 
just talked about.

### Using the Model Binding Plugin

Let us now see how we can implement all what we talked about.

#### Import a TF Lite Model

So the first step is to import a TensorFlow Lite model with metadata. 
Android Studio now has a new option for importing TensorFlow model, 
just right click on the module you want to import it in and you will see an 
option under `others` called `TF Lite model`. 

![](images/import-model-opt.jpg)

You can now just pass in path of your `tflite` model, it will import the model 
for you in a directory in the module you selected earlier called `ml` from where
you will be able to use the model. Adding the dependencies and GPU acceleration 
too is just a click away.

![](images/import-model-dialog-box.jpg)

So now from my model metadata I can also know the input, output shapes and a 
lot more that I would need to use it, you can see this info by opening the 
`tflite` model file in Android Studio. So in this screenshot I am using an 
open-source model made by me to classify between rock, paper and scissors. So 
you just show your hand in front of the camera and it identifies if it's a rock 
paper or scissor, and that's what I demonstrate here too.

![](images/model-metadata.jpg)
