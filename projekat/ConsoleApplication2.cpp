#include <iostream>
#include <fstream>
#include <sstream>

#include <emmintrin.h>
#include <immintrin.h>
#include "_Timer.h"

using namespace std;

/* Function to sort an array using insertion sort*/
void insertionSort(int arr[], int n)
{
	int i, key, j;
	for (i = 1; i < n; i++)
	{
		key = arr[i];
		j = i - 1;

		/* Move elements of arr[0..i-1], that are
		greater than key, to one position ahead
		of their current position */
		while (j >= 0 && arr[j] > key)
		{
			arr[j + 1] = arr[j];
			j = j - 1;
		}
		arr[j + 1] = key;
	}
}


int main()
{
	int array[280][280];
	int arr[280][280];
	int arrSIMD[280][280];

	int window[9], row = 0, col = 0, numrows = 0, numcols = 0, MAX = 0;

	numcols = 260;
	numrows = 260;

	srand(time(NULL));


	for (row = 0; row <= numrows; ++row)
			array[row][0] = 0;
	for (col = 0; col <= numcols; ++col)
		array[0][col] = 0;


	for (row = 1; row <= numrows; ++row)
	{
		for (col = 1; col <= numcols; ++col)
		{
			 array[row][col]= rand() % 256;
		}
	}


	StartTimer(NOSIMD);


	for (row = 1; row <= numrows; ++row)
	{
		for (col = 1; col <= numcols; ++col)
		{

			window[0] = array[row - 1][col - 1];
			window[1] = array[row - 1][col];
			window[2] = array[row - 1][col + 1];
			window[3] = array[row][col - 1];
			window[4] = array[row][col];
			window[5] = array[row][col + 1];
			window[6] = array[row + 1][col - 1];
			window[7] = array[row + 1][col];
			window[8] = array[row + 1][col + 1];


			insertionSort(window, 9);

			arr[row][col] = window[4];
		}
	}

	EndTimer

	StartTimer(SIMD);

	for (row = 1; row <= numrows; ++row)
	{

		for (col = 1; col <= numcols-8; col+=8)
		{

			__m256i v1 = _mm256_loadu_si256((__m256i const*)(array[row - 1] + col - 1));
			__m256i v2 = _mm256_loadu_si256((__m256i const*)(array[row - 1] + col));
			__m256i v3 = _mm256_loadu_si256((__m256i const*)(array[row - 1] + col + 1));
			__m256i v4 = _mm256_loadu_si256((__m256i const*)(array[row] + col - 1));
			__m256i v5 = _mm256_loadu_si256((__m256i const*)(array[row] + col));
			__m256i v6 = _mm256_loadu_si256((__m256i const*)(array[row] + col + 1));
			__m256i v7 = _mm256_loadu_si256((__m256i const*)(array[row + 1] + col - 1));
			__m256i v8 = _mm256_loadu_si256((__m256i const*)(array[row + 1] + col));
			__m256i v9 = _mm256_loadu_si256((__m256i const*)(array[row + 1] + col + 1));
			__m256i v1p;

			v1p = _mm256_max_epi32(v1, v2);
			v2 = _mm256_min_epi32(v1, v2);
			v1 = _mm256_max_epi32(v1p, v3);
			v3 = _mm256_min_epi32(v1p, v3);
			v1p = _mm256_max_epi32(v1, v4);
			v4 = _mm256_min_epi32(v1, v4);
			v1 = _mm256_max_epi32(v1p, v5);
			v5 = _mm256_min_epi32(v1p, v5);
			v1p = _mm256_max_epi32(v1, v6);
			v6 = _mm256_min_epi32(v1, v6);
			v1 = _mm256_max_epi32(v1p, v7);
			v7 = _mm256_min_epi32(v1p, v7);
			v1p = _mm256_max_epi32(v1, v8);
			v8 = _mm256_min_epi32(v1, v8);
			v1 = _mm256_max_epi32(v1p, v9);
			v9 = _mm256_min_epi32(v1p, v9);
			///v1 - sort window[8]
			v1p = _mm256_max_epi32(v2, v3);
			v3 = _mm256_min_epi32(v2, v3);
			v2 = _mm256_max_epi32(v1p, v4);
			v4 = _mm256_min_epi32(v1p, v4);
			v1p = _mm256_max_epi32(v2, v5);
			v5 = _mm256_min_epi32(v2, v5);
			v2 = _mm256_max_epi32(v1p, v6);
			v6 = _mm256_min_epi32(v1p, v6);
			v1p = _mm256_max_epi32(v2, v7);
			v7 = _mm256_min_epi32(v2, v7);
			v2 = _mm256_max_epi32(v1p, v8);
			v8 = _mm256_min_epi32(v1p, v8);
			v1p = _mm256_max_epi32(v2, v9);
			v9 = _mm256_min_epi32(v2, v9);
			///v1p - window[7] //nebitno
			v1p = _mm256_max_epi32(v3, v4);
			v4 = _mm256_min_epi32(v3, v4);
			v3 = _mm256_max_epi32(v1p, v5);
			v5 = _mm256_min_epi32(v1p, v5);
			v1p = _mm256_max_epi32(v3, v6);
			v6 = _mm256_min_epi32(v3, v6);
			v3 = _mm256_max_epi32(v1p, v7);
			v7 = _mm256_min_epi32(v1p, v7);
			v1p = _mm256_max_epi32(v3, v8);
			v8 = _mm256_min_epi32(v3, v8);
			v3 = _mm256_max_epi32(v1p, v9);
			v9 = _mm256_min_epi32(v1p, v9);
			///v3 - window[6]
			v1p = _mm256_max_epi32(v4, v5);
			v5 = _mm256_min_epi32(v4, v5);
			v4 = _mm256_max_epi32(v1p, v6);
			v6 = _mm256_min_epi32(v1p, v6);
			v1p = _mm256_max_epi32(v4, v7);
			v7 = _mm256_min_epi32(v4, v7);
			v4 = _mm256_max_epi32(v1p, v8);
			v8 = _mm256_min_epi32(v1p, v8);
			v1p = _mm256_max_epi32(v4, v9);
			v9 = _mm256_min_epi32(v4, v9);
			///v1p - window[5] //nebitno
			v1p = _mm256_max_epi32(v5, v6);
			v6 = _mm256_min_epi32(v5, v6);
			v5 = _mm256_max_epi32(v1p, v7);
			v7 = _mm256_min_epi32(v1p, v7);
			v1p = _mm256_max_epi32(v5, v8);
			v8 = _mm256_min_epi32(v5, v8);
			v5 = _mm256_max_epi32(v1p, v9);
			//v9 = _mm256_min_epi32(v5, v9);
			///v5 - window[4]

			

			_mm256_store_si256((__m256i*)(arrSIMD[row] + col), v5);
		}

		for (; col <= numcols; ++col) {

			window[0] = array[row - 1][col - 1];
			window[1] = array[row - 1][col];
			window[2] = array[row - 1][col + 1];
			window[3] = array[row][col - 1];
			window[4] = array[row][col];
			window[5] = array[row][col + 1];
			window[6] = array[row + 1][col - 1];
			window[7] = array[row + 1][col];
			window[8] = array[row + 1][col + 1];


			insertionSort(window, 9);

			arrSIMD[row][col] = window[4];
		}
	}
	

	EndTimer


	for (row = 1; row <= numrows; ++row)
	{
		for (col = 1; col <= numcols; ++col)
		{
			if (arr[row][col] != arrSIMD[row][col]) {
				cout << "NISU ISTI" << endl;
				return 0;
			}
		}
	}
	cout << "ISTI"<< endl;

	return 0;
}
