using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace WindowsFormsApp4
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
            LoadingFile();
            //심명화
            RIGHT_X = LEFT_X + 20;
            RIGHT_Y = LEFT_Y + 20;

            //for (int i = 1; i <= 50; i++)
            //{

            //    Console.WriteLine((i - 1) / 5);
            //}
            ////Roi(bitmap, LEFT_X, LEFT_Y, RIGHT_X, RIGHT_Y);
        }
        String Path= @"C:\\Users\\Kjy\\Documents\\새 폴더\\text.txt";
        String textValue;
        Bitmap bitmap;
        //size 26,24
        int LEFT_X = 0;
        int LEFT_Y = 20;
        int RIGHT_X;
        int RIGHT_Y;
        //0
        //int LEFT_Y = 8;      

        //1
        //int LEFT_Y = 39;

        //2
        //int LEFT_Y = 73;

        //3    
        //int LEFT_Y = 108;

        //4
        //int LEFT_Y = 140;

        //5
        //int LEFT_Y = 172;

        //6
        //int LEFT_Y = 205;

        //7
        //int LEFT_Y = 240;

        //8
        //int LEFT_Y = 273;

        //9
        //int LEFT_Y = 307;


        private void LoadingFile()
        {
            string originalFile = "사진.png";
            Image img1 = Image.FromFile(originalFile);
            bitmap = new Bitmap(img1);
            pictureBox1.Image = new Bitmap(bitmap, bitmap.Width, bitmap.Height);
        }


        private void button1_Click(object sender, EventArgs e)
        {
            LEFT_X -= 20;
            RIGHT_X -= 20;
 
           // Roi(bitmap, LEFT_X, LEFT_Y, RIGHT_X, RIGHT_Y);
        }

        private void button2_Click(object sender, EventArgs e)
        {
            LEFT_X += 20;
            RIGHT_X += 20;
   
           // Roi(bitmap, LEFT_X, LEFT_Y, RIGHT_X, RIGHT_Y);
        }

        void Roi(Bitmap ROI_Bitmap, int LEFT_X, int LEFT_Y, int RIGHT_X, int RIGHT_Y,int Num)
        {
            Rectangle cloneRect = new Rectangle(LEFT_X, LEFT_Y, RIGHT_X - LEFT_X, RIGHT_Y - LEFT_Y);
            ROI_Bitmap = new Bitmap(ROI_Bitmap.Clone(cloneRect, ROI_Bitmap.PixelFormat));

            for (int y = 0; y < ROI_Bitmap.Height; y++)
            {
                for (int x = 0; x < ROI_Bitmap.Width; x++)
                {
                    Color color = ROI_Bitmap.GetPixel(x, y);
                    if (color.R > 128)
                    {
                        textValue = "1,";
                        System.IO.File.AppendAllText(Path, textValue, Encoding.Default);
                        //Console.Write("{0},", 1);
                    }
                    else
                    {
                        textValue = "0,";
                        System.IO.File.AppendAllText(Path, textValue, Encoding.Default);
                        //Console.Write("{0},", 0);
                    }
                }
            }
            textValue = Num.ToString() + "\n";
            
            //Console.Write("zero");
            //Console.WriteLine("{0}", ROI_Bitmap.Width * ROI_Bitmap.Height);
            //Console.WriteLine();
            System.IO.File.AppendAllText(Path, textValue, Encoding.Default);
            pictureBox2.Image = new Bitmap(ROI_Bitmap, ROI_Bitmap.Width, ROI_Bitmap.Height);

        }
         
        private void button3_Click(object sender, EventArgs e)
        {
            for (int i = 0; i < 50; i++)
            {
                for (int j = 0; j< 100; j++)
                {
                    int LEFT_X = 0 * j;
                    int LEFT_Y = 20 * i;
                    RIGHT_X = LEFT_X + 20;
                    RIGHT_Y = LEFT_Y + 20;
                    Roi(bitmap, LEFT_X, LEFT_Y, RIGHT_X, RIGHT_Y,(i-1)/5);
                }
            }
        }
    }
}
