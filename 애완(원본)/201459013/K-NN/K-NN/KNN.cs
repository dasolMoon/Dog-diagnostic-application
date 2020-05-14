using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace K_NN
{
    class KNN
    {
        int lines;

        // 훈련 데이터의 값
        List<double[]> trainingSetValues = new List<double[]>();
        // 숫자 값
        List<string> trainingSetClasses = new List<string>();

        //테스트 값의 데이터
        List<double[]> testSetValues = new List<double[]>();

        int K;//K의 값

        //테스트 데이터 받기
        public void Training(double[] Number)
        {
            testSetValues.Add(Number);
        }

        //훈련용 데이터 불러오기
        public void LoadData(string path)
        {
            StreamReader file = new StreamReader(path);
            string line;

            lines = 0;

            while ((line = file.ReadLine()) != null)
            {
                // 각 문장을 분리
                string[] splitLine = line.Split(',').ToArray();

                // 리스트에 추가
                List<string> lineItems = new List<string>(splitLine.Length);
                lineItems.AddRange(splitLine);

                // 문장을 저장하는 배열
                double[] lineDoubles = new double[lineItems.Count - 1];
                // 클래스를 포함하는 문자열
                string lineClass = lineItems.ElementAt(lineItems.Count - 1);
                
                for (int i = 0; i < lineItems.Count - 1; i++)    // last item is the set class
                {
                    double val = Double.Parse(lineItems.ElementAt(i));
                    lineDoubles[i] = val;
                }

                trainingSetValues.Add(lineDoubles);
                trainingSetClasses.Add(lineClass);

                this.lines++;
            }
            file.Close();
        }

        public String Classify(int neighborsNumber, TextBox textBox1)
        {
            K = neighborsNumber;
            double[][] distances = new double[trainingSetValues.Count][];
            String[] num = new string[neighborsNumber];
            double testNumber = 0;
            int max = neighborsNumber + 1;
            int[] M = new int[K];
            int temp = 0;
            int count;

            for (int i = 0; i < trainingSetValues.Count; i++)
                distances[i] = new double[2];

            // 계산 시작
            for (var test = 0; test < testSetValues.Count; test++)
            {
                count = 0;
                Parallel.For(0, trainingSetValues.Count, index =>//거리 값
                {
                    var dist = EuclideanDistance(testSetValues[test], trainingSetValues[index]);
                    distances[index][0] = dist;
                    distances[index][1] = index;
                }
                );

                var sortedDistances = distances.AsParallel().OrderBy(t => t[0]).Take(K);//거리 값 오름차순 정렬
                textBox1.Clear();
                foreach (var d in sortedDistances)//k값 크기만큼 이웃 구하기
                {
                    string predictedClass = trainingSetClasses[(int)d[1]];                  
                    textBox1.AppendText(predictedClass + " \n");
                    num[count] = predictedClass;
                    testNumber++;
                    count++;          
                }
            }
            //가장 많은 이웃 찾기

            for (int i = 0; i < K; i++)
            {
                for (int j = i; j < K; j++)
                {
                    if (num[i] == num[j])
                    {
                        M[i]++;
                    }
                }
            }

            for (int i = 0; i < K; i++)
            {
                if (M[i] > max)
                {
                    temp = i;
                    max = M[i];
                }
            }

            return num[temp];
        }
        private static double EuclideanDistance(double[] sampleOne, double[] sampleTwo)//거리 값
        {
            double d = 0.0;

            for (int i = 0; i < sampleOne.Length; i++)
            {
                double temp = sampleOne[i] - sampleTwo[i];
                d += temp * temp;
            }
            return Math.Sqrt(d);
        }
    }
}
