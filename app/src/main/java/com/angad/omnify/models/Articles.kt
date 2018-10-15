package com.angad.omnify.models

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

open class Article: RealmObject(){
        /**
         * by : BobbyVsTheDevil
         * descendants : 32
         * id : 18159481
         * kids : [18159669,18159650,18160086,18160191,18159654,18159996,18160106,18160040,18160252,18159909]
         * score : 126
         * time : 1538901024
         * title : Recovering Emotions After 24 Years on Antidepressants
         * type : story
         * url : https://www.madinamerica.com/2018/10/recovering-emotions-24-years-antidepressants/
         */

        var by: String? = null
        var descendants: Int = 0
        @PrimaryKey var id: Int = 0
        var score: Int = 0
        var time: Int = 0
        var title: String? = null
        var type: String? = null
        var url: String? = null
        var kids: RealmList<Int>? = null
}

open class Comment: RealmObject() {
        /**
         * by : psergeant
         * id : 18159669
         * kids : [18160176,18159834,18160019,18159823,18160013,18160133]
         * parent : 18159481
         * text : The only thing that seems to be universally true about mental health is that we’re all different, and someone else’s experiences rarely apply perfectly to your own.
         *
         *I put off getting on the meds for at least 15 years longer than I should have done because of stories like this. They have been life changing lot positive for me with almost no downsides.
         *
         *In addition, I’ve read more than one story like this where the person eventually decides it’s time to get back on the SSRIs after a year or two off.
         *
         *Experiment, find what works for you, but these comments that are angry at big pharma and describe pills as primarily bad need to be seen very much as just localised experiences.
         * time : 1538904385
         * type : comment
         */

        var by: String? = null
        @PrimaryKey var id: Int = 0
        var parent: Int = 0
        var text: String? = null
        var time: Int = 0
        var type: String? = null
        var kids: RealmList<Int>? = null
}