package com.example.seyahathanem.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.mezunproject.R
import com.example.mezunproject.databinding.FragmentAddPlaceBinding
import com.example.mezunproject.databinding.FragmentCategoryBinding
import com.example.seyahathanem.viewModel.ImageKeeper
import java.io.ByteArrayOutputStream


class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var bitmap: Bitmap
    private lateinit var stream: ByteArrayOutputStream
    private  var encodedImage : String ? = null
    private  var categoryName: String? = null
    private lateinit var viewModel: ImageKeeper



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ImageKeeper::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root




    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.Restaurants.setOnClickListener {

            categorySelect(binding.restaurantsImage)
            categoryName = binding.categoryNameRestaurants.text.toString()

            actionToAddPlaceFragment(it)
        }

        binding.Entertainment.setOnClickListener {

            categorySelect(binding.entertainmentImage)
            categoryName = binding.categoryNameEntertainment.text.toString()
            actionToAddPlaceFragment(it)

        }

        binding.Cars.setOnClickListener {

            //alertDialog.setTitle()
            categorySelect(binding.carsImage)
            categoryName = binding.categoryNameCars.text.toString()
            actionToAddPlaceFragment(it)

        }
        binding.Shopping.setOnClickListener {

            categorySelect(binding.imageShopping)
            categoryName = binding.categoryNameShpping.text.toString()
            actionToAddPlaceFragment(it)
        }



        binding.backPressed.setOnClickListener {

            val action = CategoryFragmentDirections.actionCategoryFragmentToAddPlaceFragment()
            Navigation.findNavController(it).navigate(action)

        }


        binding.backPressed.setOnClickListener {



            //encodedImage: an image converted to string from byteArray
            //actionToUploadFragment(it)
            val action = CategoryFragmentDirections.actionCategoryFragmentToAddPlaceFragment()
            Navigation.findNavController(it).navigate(action)


        }
    }

    private fun actionToAddPlaceFragment(view: View){

        val action = CategoryFragmentDirections.actionCategoryFragmentToAddPlaceFragment(encodedImage,categoryName)
        Navigation.findNavController(view).navigate(action)
    }

    fun bitmapImage(image: Bitmap, maxSize: Int) : Bitmap {

        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1) {
            //yatay
            width = maxSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }else {
            //dikey
            height = maxSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)

    }

    private fun categorySelect(imageView: ImageView){

        imageView.isDrawingCacheEnabled = true
        stream = ByteArrayOutputStream()
        bitmap = imageView.drawingCache
        val smallBitmap = bitmapImage(bitmap,150)
        smallBitmap.compress(Bitmap.CompressFormat.PNG,100,stream)
        val byteArray = stream.toByteArray()
        encodedImage = Base64.encodeToString(byteArray,Base64.DEFAULT)
        binding.restaurantsImage.isDrawingCacheEnabled = false

    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}