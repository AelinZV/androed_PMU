package com.example.myapplication

import android.widget.ImageView

data class Bug(

    // Уникальный идентификатор насекомого
    val id: Int,

    // Изображение насекомого
    val imageView: ImageView,

    // Текущая позиция
    var x: Float,
    var y: Float,

    // Скорость и направление движения
    var dx: Float,
    var dy: Float,

    // Размер изображения
    val size: Int,

    // Тип насекомого
    val type: BugType,

    // Количество очков за попадание
    val points: Int
)