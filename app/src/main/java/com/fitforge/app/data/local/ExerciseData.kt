package com.fitforge.app.data.local

import com.fitforge.app.data.models.Exercise

object ExerciseData {
    
    data class ExerciseCategory(
        val name: String,
        val imageUrl: String
    )

    val categories = listOf(
        ExerciseCategory("Full Body", "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800&q=80"),
        ExerciseCategory("Chest", "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800&q=80"),
        ExerciseCategory("Back", "https://images.unsplash.com/photo-1603287681836-b174ce5074c2?w=800&q=80"),
        ExerciseCategory("Legs", "https://images.unsplash.com/photo-1434682881908-b43d0467b798?w=800&q=80"),
        ExerciseCategory("Shoulders", "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=800&q=80"),
        ExerciseCategory("Arms", "https://images.unsplash.com/photo-1581009137042-c552e485697a?w=800&q=80"),
        ExerciseCategory("Abs", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&q=80")
    )

    val exercises = listOf(
        // Chest
        Exercise(
            id = "c1",
            name = "Barbell Bench Press",
            bodyPart = "Chest",
            target = "Pectorals",
            equipment = "Barbell",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bench-Press.gif",
            instructions = listOf("Lie flat on a bench", "Grip the barbell slightly wider than shoulder-width", "Lower the bar to your mid-chest", "Push the bar back up to the starting position"),
            secondaryMuscles = listOf("Triceps", "Shoulders")
        ),
        Exercise(
            id = "c2",
            name = "Push-Up",
            bodyPart = "Chest",
            target = "Pectorals",
            equipment = "Bodyweight",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Push-Up.gif",
            instructions = listOf("Start in a plank position", "Lower your body until your chest is close to the floor", "Push yourself back up"),
            secondaryMuscles = listOf("Triceps", "Shoulders", "Core")
        ),
        Exercise(
            id = "c3",
            name = "Incline Dumbbell Press",
            bodyPart = "Chest",
            target = "Upper Pectorals",
            equipment = "Dumbbell",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Incline-Dumbbell-Press.gif",
            instructions = listOf("Lie on an incline bench", "Press dumbbells up until your arms are straight", "Lower them slowly back down"),
            secondaryMuscles = listOf("Triceps", "Front Deltoids")
        ),
        
        // Back
        Exercise(
            id = "b1",
            name = "Pull-Up",
            bodyPart = "Back",
            target = "Latissimus Dorsi",
            equipment = "Bodyweight",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Pull-up.gif",
            instructions = listOf("Grab the pull-up bar with an overhand grip", "Pull yourself up until your chin is over the bar", "Lower yourself slowly"),
            secondaryMuscles = listOf("Biceps", "Forearms")
        ),
        Exercise(
            id = "b2",
            name = "Barbell Row",
            bodyPart = "Back",
            target = "Latissimus Dorsi",
            equipment = "Barbell",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bent-Over-Row.gif",
            instructions = listOf("Hinge at the hips", "Keep your back straight", "Pull the barbell towards your waist", "Lower it in a controlled manner"),
            secondaryMuscles = listOf("Biceps", "Rear Deltoids", "Lower Back")
        ),
        
        // Legs
        Exercise(
            id = "l1",
            name = "Barbell Squat",
            bodyPart = "Legs",
            target = "Quadriceps",
            equipment = "Barbell",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/BARBELL-SQUAT.gif",
            instructions = listOf("Stand with feet shoulder-width apart", "Rest the barbell on your upper back", "Squat down by bending your knees and hips", "Push back up to the starting position"),
            secondaryMuscles = listOf("Glutes", "Hamstrings", "Core")
        ),
        Exercise(
            id = "l2",
            name = "Romanian Deadlift",
            bodyPart = "Legs",
            target = "Hamstrings",
            equipment = "Barbell",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Romanian-Deadlift.gif",
            instructions = listOf("Hold barbell with an overhand grip", "Hinge at the hips while keeping your legs mostly straight", "Lower the bar until you feel a stretch in your hamstrings", "Return to standing"),
            secondaryMuscles = listOf("Glutes", "Lower Back")
        ),
        
        // Shoulders
        Exercise(
            id = "s1",
            name = "Overhead Press",
            bodyPart = "Shoulders",
            target = "Deltoids",
            equipment = "Barbell",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Overhead-Press.gif",
            instructions = listOf("Stand with the barbell resting on your front shoulders", "Press the bar straight overhead", "Lower it slowly back to your shoulders"),
            secondaryMuscles = listOf("Triceps", "Upper Chest")
        ),
        Exercise(
            id = "s2",
            name = "Lateral Raise",
            bodyPart = "Shoulders",
            target = "Lateral Deltoids",
            equipment = "Dumbbell",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Lateral-Raise.gif",
            instructions = listOf("Stand holding a dumbbell in each hand", "Raise your arms out to the sides until they are parallel to the floor", "Lower them slowly"),
            secondaryMuscles = emptyList()
        ),
        
        // Arms
        Exercise(
            id = "a1",
            name = "Bicep Curl",
            bodyPart = "Arms",
            target = "Biceps",
            equipment = "Dumbbell",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Curl.gif",
            instructions = listOf("Stand holding a dumbbell in each hand", "Curl the weights towards your shoulders", "Squeeze your biceps at the top", "Lower slowly"),
            secondaryMuscles = listOf("Forearms")
        ),
        Exercise(
            id = "a2",
            name = "Tricep Extension",
            bodyPart = "Arms",
            target = "Triceps",
            equipment = "Cable",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Pushdown.gif",
            instructions = listOf("Stand in front of a cable machine", "Use a rope attachment", "Push the rope down until your arms are fully extended", "Return slowly"),
            secondaryMuscles = emptyList()
        ),
        
        // Abs
        Exercise(
            id = "ab1",
            name = "Crunch",
            bodyPart = "Abs",
            target = "Rectus Abdominis",
            equipment = "Bodyweight",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2015/11/Crunch.gif",
            instructions = listOf("Lie on your back with knees bent", "Place your hands behind your head", "Lift your upper body towards your knees", "Lower slowly"),
            secondaryMuscles = emptyList()
        ),
        Exercise(
            id = "ab2",
            name = "Plank",
            bodyPart = "Abs",
            target = "Core",
            equipment = "Bodyweight",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Front-Plank.gif",
            instructions = listOf("Start in a push-up position, but rest on your forearms", "Keep your body in a straight line", "Hold the position"),
            secondaryMuscles = listOf("Shoulders", "Back")
        ),
        
        // Full Body
        Exercise(
            id = "fb1",
            name = "Burpee",
            bodyPart = "Full Body",
            target = "Cardio",
            equipment = "Bodyweight",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Burpees.gif",
            instructions = listOf("Start from a standing position", "Drop into a squat position and place your hands on the ground", "Kick your feet back to a plank", "Return to the squat position", "Stand up from the squat position"),
            secondaryMuscles = listOf("Chest", "Legs", "Core")
        )
    )

    fun getExercisesByCategory(category: String): List<Exercise> {
        if (category == "Full Body") {
            return exercises.filter { it.bodyPart == "Full Body" }
        }
        return exercises.filter { it.bodyPart == category }
    }
}
