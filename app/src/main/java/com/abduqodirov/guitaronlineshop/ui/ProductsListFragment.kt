package com.abduqodirov.guitaronlineshop.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.abduqodirov.guitaronlineshop.R
import com.abduqodirov.guitaronlineshop.adapter.ProductsRecyclerAdapter
import com.abduqodirov.guitaronlineshop.databinding.FragmentProductsListBinding
import com.abduqodirov.guitaronlineshop.model.FetchingProduct
import com.abduqodirov.guitaronlineshop.model.Product
import com.abduqodirov.guitaronlineshop.network.Status.ERROR
import com.abduqodirov.guitaronlineshop.network.Status.LOADING
import com.abduqodirov.guitaronlineshop.network.Status.SUCCESS
import com.abduqodirov.guitaronlineshop.viewmodel.ProductsViewModel

class ProductsListFragment : Fragment() {

    private var _binding: FragmentProductsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProductsListBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.refreshProducts()

        observeProductsData()

        setUpViewListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun observeProductsData() {
        viewModel.products.observe(
            viewLifecycleOwner,
            {

                it?.let { response ->
                    when (response.status) {

                        SUCCESS -> {
                            response.data.let { nullableProducts ->

                                nullableProducts?.let { nullSafeProducts ->
                                    populateViewsWithSuccessfullyFetchedData(nullSafeProducts)
                                }
                            }
                        }

                        ERROR -> {
                            switchUIToErrorState()
                        }

                        LOADING -> {
                            switchUIToLoadingState()
                        }
                    }
                }
            }
        )
    }

    private fun populateViewsWithSuccessfullyFetchedData(products: List<Product>) {

        switchUIToSuccessState()

        if (products.isEmpty()) {
            binding.productsMessageTxt.text =
                getString(R.string.no_products)
            binding.productsMessageTxt.visibility = View.VISIBLE
        } else {

            val productAdapter = ProductsRecyclerAdapter(
                ProductsRecyclerAdapter.ProductClickListener {

                    navigateToProductDetails(it)
                }
            )

            binding.productsRecycler.adapter = productAdapter
            binding.productsRecycler.setHasFixedSize(true)

            productAdapter.submitList(products.reversed())
        }
    }

    private fun switchUIToLoadingState() {
        binding.productsProgressBar.visibility = View.VISIBLE

        binding.productsRecycler.visibility = View.GONE
        binding.productsRetryButton.visibility = View.GONE
        binding.productsMessageTxt.visibility = View.GONE
    }

    private fun switchUIToSuccessState() {
        binding.productsRecycler.visibility = View.VISIBLE

        binding.productsProgressBar.visibility = View.GONE
        binding.productsRetryButton.visibility = View.GONE
        binding.productsMessageTxt.visibility = View.GONE
    }

    private fun switchUIToErrorState() {
        binding.productsRecycler.visibility = View.INVISIBLE
        binding.productsProgressBar.visibility = View.INVISIBLE

        binding.productsMessageTxt.text = getString(R.string.product_fetching_failure)
        binding.productsRetryButton.visibility = View.VISIBLE
        binding.productsMessageTxt.visibility = View.VISIBLE
    }

    private fun navigateToProductDetails(it: Product) {
        findNavController().navigate(
            ProductsListFragmentDirections.actionProductsListFragmentToProductDetailsFragment(
                (it as FetchingProduct).id
            )
        )
    }

    private fun setUpViewListeners() {
        binding.productsRetryButton.setOnClickListener {
            viewModel.refreshProducts()
        }

        binding.productsAddNewProductBtn.setOnClickListener {
            findNavController().navigate(
                ProductsListFragmentDirections.actionProductsListFragmentToSubmitNewProductFragment()
            )
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProductsListFragment()
    }
}
