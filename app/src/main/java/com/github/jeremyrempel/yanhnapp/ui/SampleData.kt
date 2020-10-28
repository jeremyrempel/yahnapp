package com.github.jeremyrempel.yanhnapp.ui

import com.github.jeremyrempel.yahn.Comment
import com.github.jeremyrempel.yahn.Post
import java.time.Instant
import java.time.temporal.ChronoUnit

object SampleData {
    val posts = listOf(
        Post(
            1,
            "Jetpack Compose 1.0 released",
            "developer.android.com",
            "https://developer.android.com",
            null,
            96,
            2,
            9,
            0,
            0,
            0,
            0
        ),
        Post(
            1,
            "First Man on Mars. This is a super long title that should go over the maximum line length.",
            "nasa.gov",
            "https://nasa.gov",
            null,
            1000,
            5,
            1000,
            1,
            1,
            0,
            0
        ),
        Post(
            1,
            "KMM 1.0.0 released",
            "kotlinlang.org",
            "https://kotlinlang.org",
            null,
            100,
            1,
            50,
            1,
            0,
            0,
            0
        ),
        Post(
            1,
            "Jetpack Compose is Awesome",
            "medium.com",
            "https://medium.com",
            null,
            50,
            1,
            50,
            0,
            0,
            0,
            0
        ),
        Post(
            1,
            "Linus Torvalids announces presidential candidacy",
            "cnn.com",
            "https://cnn.com",
            null,
            125,
            10,
            100,
            0,
            1,
            0,
            0
        ),
        Post(
            1,
            "Ask HN: How can I learn to code?",
            null,
            null,
            "<p>How can I learn to code. This is a long description with html</p>",
            200,
            5,
            25,
            0,
            0,
            0,
            0
        )
    )

    val commentList = listOf(
        Comment(
            id = 1,
            username = "En",
            unixTime = Instant.now().minus(10, ChronoUnit.MINUTES).epochSecond,
            content = "&#34;Lorem Ipsum&&#34;<p> is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            postid = 1,
            childrenCnt = 2,
            created = 0,
            lastUpdated = 0,
            parent = null,
            sortorder = 1
        )

        // Comment(
        //     "En",
        //     6,
        //     "&#34;Lorem Ipsum&&#34;<p> is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        //     listOf(
        //         Comment(
        //             "Kaiman",
        //             6,
        //             "L2: Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        //             listOf(
        //                 Comment(
        //                     "Nikaido",
        //                     4,
        //                     "L3: I'm a short one liner reply",
        //                     listOf(
        //                         Comment(
        //                             "Shen",
        //                             10,
        //                             "L3: Nikaido. Are you a sorceror?"
        //                         )
        //                     )
        //                 ),
        //                 Comment(
        //                     "Ebisu",
        //                     1,
        //                     "L3: Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        //                 )
        //             )
        //         ),
        //     )
        // ),
    )
}
