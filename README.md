# EasySlider [![](https://jitpack.io/v/eurigo/EasySlider.svg)](https://jitpack.io/#eurigo/EasySlider)

支持各种样式自定义的滑动条

* 支持设置图标 
* 支持设置进度文本格式 
* 支持图标和进度文本关联或分开
* 支持轨道圆角、渐变色

---

### 预览

![image](https://github.com/eurigo/EasySlider/assets/18246136/c01d6277-d04f-4921-a68a-f6aec7ef4d80)


### 快速使用

#### 1.在项目级 `settings.gradle`添加：

```groovy
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```

#### 2.在app模块下的`build.gradle`文件中加入：

```groovy
dependencies {
    implementation 'com.github.eurigo:EasySlider:1.0.0'
}
```

---

### xml属性

| 名称                    |               说明                |
| :---------------------- | :-------------------------------: |
|minValue                 |最小值，默认0                       |
|maxValue|最大值，默认100|
|value|当前值|
|showProgressText|是否显示进度文本，默认显示|
|progressTextColor|进度文本颜色，默认白色|
|progressTextSize|进度文本大小，默认16sp|
|progressTextFormat|进度文本的格式, 默认整型|
|progressTextPadding|进度文本与轨道的间距, 默认0, TextGravity设置center时，此参数无效|
|progressTextGravity|进度文本位置, 默认轨道居中显示|
|trackIcon|轨道上的图标|
|trackIconSize|轨道上的图标大小，宽=高, 默认16dp|
|trackIconTint|轨道上的图标颜色，仅适用纯色图片|
|trackIconPadding|轨道上的图标与文本的间距, 默认4dp|
|trackIconGravity|轨道上图标位置，默认显示在文本之前, 如果跟随文本, 文本不显示时, 图标也会不显示|
|trackActiveColor|轨道的激活颜色，默认#FFFFFF|
|trackActiveGradientColor|轨道的激活渐变色，设置渐变色会覆盖激活颜色，格式：#36D1DC,#5B86E5|
|trackInactiveColor|轨道的非激活颜色，默认#757575|
|trackInactiveGradientColor|轨道的非激活渐变色，设置渐变色色会覆盖激活颜色，格式：#36D1DC,#5B86E5|
|trackRadius|轨道的圆角, 默认12dp, 有圆角时, 激活轨道最小宽度时圆角的2倍|
|keepMinWidth|激活轨道保留的最小宽度，默认0|
|showThumb|是否显示指示标志，默认显示|
|thumbRadius|标志圆角, 默认是标志高度的一半|
|thumbPadding|标志的Padding, 默认4dp|
|thumbColor|标志颜色, 默认白色|
|thumbWidth|标志宽度, 默认24dp|
|thumbHeight|标志高度, 默认24dp|

### 更多

请参考demo
