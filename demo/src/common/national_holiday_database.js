//*祝日テーブル(仮)
//H220625
//*（アクセス方法)nHoliday配列を作って                                          
//           nHoliday[2010][1][0]       =   1                           
//                     ↑  ↑  ↑           ↑                          
//                     年  月  祝日１つ目   値( 2010年１月の最初の祝日は1日)
//の形でアクセスをする
//*(前提）
// ①myCalendar.htmlで表示可能な範囲の年分(1870年くらい～2025まで）は
//  nHoliday[2010][1]までの空配列は無いとエラーになってしまうので空配列で構わないので読み込む前に用意しておく
//   (エラーを回避できれば必要ないんですが…）
// ②実際にmycalendar.html(or mycalendar2.html)ではnHoliday全配列を読み込んだ後にnHoliday[year][month]分だけ取出し、その[year]の[month]にある全日にち分(2月以外は30日か31日)をループさせて
//  nHoliday[year][month]配列に存在する値と合致した日に赤色文字にしています。    

//(未完）※nHoliday[2010][12]までは定義をしておかないと定義していない年月に関してはとまってしまう
//            →とりあえず空の配列をたくさん作って回避(h22.06.26)
//       ※実際にはDB2にデータベース登録してajax or jquery経由アクセスのほうがいいと思う

//2010年度
var nHoliday , p , q ; 
//祝日配列
 nHoliday = new Array();
 p=1800; q=2100;
//祝日配列 -- 年月  //＜－ココは空配列として作っておかないとエラーになってしまうので作るところ（最低マイカレの表示可能域分は必要)
 for (p ; p <= q; p++){ 
 nHoliday[p] = new Array(1,2,3,4,5,6,7,8,9,10,11,12);
 }
//子配列2010 （祝日がない月も配列として１つ(値は0か32以上の何か)は必要。各月5日分確保しているのは見やすくするため以上の意味はないです。）
 nHoliday[2010][1]   = new Array(1,11,0,0,0);
 nHoliday[2010][2]   = new Array(11,0,0,0,0);
 nHoliday[2010][3]   = new Array(21,22,0,0,0);
 nHoliday[2010][4]   = new Array(29,0,0,0,0);
 nHoliday[2010][5]   = new Array(3,4,5,0,0);
 nHoliday[2010][6]   = new Array(0,0,0,0,0);
 nHoliday[2010][7]   = new Array(19,0,0,0,0);
 nHoliday[2010][8]   = new Array(0,0,0,0,0);
 nHoliday[2010][9]   = new Array(20,23,0,0,0);
 nHoliday[2010][10]  = new Array(11,0,0,0,0);
 nHoliday[2010][11]  = new Array(3,23,0,0,0);
 nHoliday[2010][12]  = new Array(23,0,0,0,0);

//子配列2011 
 nHoliday[2011][1]   = new Array(1,10,0,0,0);
 nHoliday[2011][2]   = new Array(11,0,0,0,0);
 nHoliday[2011][3]   = new Array(21,0,0,0,0);
 nHoliday[2011][4]   = new Array(29,0,0,0,0);
 nHoliday[2011][5]   = new Array(3,4,5,0,0);
 nHoliday[2011][6]   = new Array(0,0,0,0,0);
 nHoliday[2011][7]   = new Array(18,0,0,0,0);
 nHoliday[2011][8]   = new Array(0,0,0,0,0);
 nHoliday[2011][9]   = new Array(19,23,0,0,0);
 nHoliday[2011][10]  = new Array(10,0,0,0,0);
 nHoliday[2011][11]  = new Array(3,23,0,0,0);
 nHoliday[2011][12]  = new Array(23,0,0,0,0);

//子配列2012 
 nHoliday[2012][1]   = new Array(1,2,9,0,0);
 nHoliday[2012][2]   = new Array(11,0,0,0,0);
 nHoliday[2012][3]   = new Array(20,0,0,0,0);
 nHoliday[2012][4]   = new Array(29,30,0,0,0);
 nHoliday[2012][5]   = new Array(3,4,5,0,0);
 nHoliday[2012][6]   = new Array(0,0,0,0,0);
 nHoliday[2012][7]   = new Array(16,0,0,0,0);
 nHoliday[2012][8]   = new Array(0,0,0,0,0);
 nHoliday[2012][9]   = new Array(17,22,0,0,0);
 nHoliday[2012][10]  = new Array(8,0,0,0,0);
 nHoliday[2012][11]  = new Array(3,23,0,0,0);
 nHoliday[2012][12]  = new Array(23,24,0,0,0);

//子配列2013 
 nHoliday[2013][1]   = new Array(1,14,0,0,0);
 nHoliday[2013][2]   = new Array(11,0,0,0,0);
 nHoliday[2013][3]   = new Array(20,0,0,0,0);
 nHoliday[2013][4]   = new Array(29,0,0,0,0);
 nHoliday[2013][5]   = new Array(3,4,5,6,0);
 nHoliday[2013][6]   = new Array(0,0,0,0,0);
 nHoliday[2013][7]   = new Array(15,0,0,0,0);
 nHoliday[2013][8]   = new Array(0,0,0,0,0);
 nHoliday[2013][9]   = new Array(16,23,0,0,0);
 nHoliday[2013][10]  = new Array(14,0,0,0,0);
 nHoliday[2013][11]  = new Array(3,4,23,0,0);
 nHoliday[2013][12]  = new Array(23,0,0,0,0);

//子配列2014 
 nHoliday[2014][1]   = new Array(1,13,0,0,0);
 nHoliday[2014][2]   = new Array(11,0,0,0,0);
 nHoliday[2014][3]   = new Array(21,0,0,0,0);
 nHoliday[2014][4]   = new Array(29,0,0,0,0);
 nHoliday[2014][5]   = new Array(3,4,5,6,0);
 nHoliday[2014][6]   = new Array(0,0,0,0,0);
 nHoliday[2014][7]   = new Array(21,0,0,0,0);
 nHoliday[2014][8]   = new Array(0,0,0,0,0);
 nHoliday[2014][9]   = new Array(15,23,0,0,0);
 nHoliday[2014][10]  = new Array(13,0,0,0,0);
 nHoliday[2014][11]  = new Array(3,23,24,0,0);
 nHoliday[2014][12]  = new Array(23,0,0,0,0);

//子配列2015 
 nHoliday[2015][1]   = new Array(1,12,0,0,0);
 nHoliday[2015][2]   = new Array(11,0,0,0,0);
 nHoliday[2015][3]   = new Array(21,0,0,0,0);
 nHoliday[2015][4]   = new Array(29,0,0,0,0);
 nHoliday[2015][5]   = new Array(3,4,5,6,0);
 nHoliday[2015][6]   = new Array(0,0,0,0,0);
 nHoliday[2015][7]   = new Array(20,0,0,0,0);
 nHoliday[2015][8]   = new Array(0,0,0,0,0);
 nHoliday[2015][9]   = new Array(21,22,23,0,0);
 nHoliday[2015][10]  = new Array(12,0,0,0,0);
 nHoliday[2015][11]  = new Array(3,23,0,0,0);
 nHoliday[2015][12]  = new Array(23,0,0,0,0);

//子配列2016 （１月あたり５日作っておく）
 nHoliday[2016][1]   = new Array(1,11,0,0,0);
 nHoliday[2016][2]   = new Array(11,0,0,0,0);
 nHoliday[2016][3]   = new Array(20,21,0,0,0);
 nHoliday[2016][4]   = new Array(29,0,0,0,0);
 nHoliday[2016][5]   = new Array(3,4,5,0,0);
 nHoliday[2016][6]   = new Array(0,0,0,0,0);
 nHoliday[2016][7]   = new Array(18,0,0,0,0);
 nHoliday[2016][8]   = new Array(0,0,0,0,0);
 nHoliday[2016][9]   = new Array(19,22,0,0,0);
 nHoliday[2016][10]  = new Array(10,0,0,0,0);
 nHoliday[2016][11]  = new Array(3,23,0,0,0);
 nHoliday[2016][12]  = new Array(23,0,0,0,0);

//子配列2017 
 nHoliday[2017][1]   = new Array(1,2,9,0,0);
 nHoliday[2017][2]   = new Array(11,0,0,0,0);
 nHoliday[2017][3]   = new Array(20,0,0,0,0);
 nHoliday[2017][4]   = new Array(29,0,0,0,0);
 nHoliday[2017][5]   = new Array(3,4,5,0,0);
 nHoliday[2017][6]   = new Array(0,0,0,0,0);
 nHoliday[2017][7]   = new Array(17,0,0,0,0);
 nHoliday[2017][8]   = new Array(0,0,0,0,0);
 nHoliday[2017][9]   = new Array(18,23,0,0,0);
 nHoliday[2017][10]  = new Array(9,0,0,0,0);
 nHoliday[2017][11]  = new Array(3,23,0,0,0);
 nHoliday[2017][12]  = new Array(23,0,0,0,0);

//子配列2018 
 nHoliday[2018][1]   = new Array(1,8,0,0,0);
 nHoliday[2018][2]   = new Array(11,12,0,0,0);
 nHoliday[2018][3]   = new Array(21,0,0,0,0);
 nHoliday[2018][4]   = new Array(29,30,0,0,0);
 nHoliday[2018][5]   = new Array(3,4,5,0,0);
 nHoliday[2018][6]   = new Array(0,0,0,0,0);
 nHoliday[2018][7]   = new Array(16,0,0,0,0);
 nHoliday[2018][8]   = new Array(0,0,0,0,0);
 nHoliday[2018][9]   = new Array(17,23,24,0,0);
 nHoliday[2018][10]  = new Array(8,0,0,0,0);
 nHoliday[2018][11]  = new Array(3,23,0,0,0);
 nHoliday[2018][12]  = new Array(23,24,0,0,0);

//子配列2019 
 nHoliday[2019][1]   = new Array(1,14,0,0,0);
 nHoliday[2019][2]   = new Array(11,0,0,0,0);
 nHoliday[2019][3]   = new Array(21,0,0,0,0);
 nHoliday[2019][4]   = new Array(29,0,0,0,0);
 nHoliday[2019][5]   = new Array(3,4,5,6,0);
 nHoliday[2019][6]   = new Array(0,0,0,0,0);
 nHoliday[2019][7]   = new Array(15,0,0,0,0);
 nHoliday[2019][8]   = new Array(0,0,0,0,0);
 nHoliday[2019][9]   = new Array(16,23,0,0,0);
 nHoliday[2019][10]  = new Array(14,0,0,0,0);
 nHoliday[2019][11]  = new Array(3,4,23,0,0);
 nHoliday[2019][12]  = new Array(23,0,0,0,0);

//子配列2020 
 nHoliday[2020][1]   = new Array(1,13,0,0,0);
 nHoliday[2020][2]   = new Array(11,0,0,0,0);
 nHoliday[2020][3]   = new Array(20,0,0,0,0);
 nHoliday[2020][4]   = new Array(29,0,0,0,0);
 nHoliday[2020][5]   = new Array(3,4,5,6,0);
 nHoliday[2020][6]   = new Array(0,0,0,0,0);
 nHoliday[2020][7]   = new Array(20,0,0,0,0);
 nHoliday[2020][8]   = new Array(0,0,0,0,0);
 nHoliday[2020][9]   = new Array(21,22,0,0,0);
 nHoliday[2020][10]  = new Array(12,0,0,0,0);
 nHoliday[2020][11]  = new Array(3,23,24,0,0);
 nHoliday[2020][12]  = new Array(23,0,0,0,0);

//子配列2021 
 nHoliday[2021][1]   = new Array(1,12,0,0,0);
 nHoliday[2021][2]   = new Array(11,0,0,0,0);
 nHoliday[2021][3]   = new Array(21,0,0,0,0);
 nHoliday[2021][4]   = new Array(29,0,0,0,0);
 nHoliday[2021][5]   = new Array(3,4,5,0,0);
 nHoliday[2021][6]   = new Array(0,0,0,0,0);
 nHoliday[2021][7]   = new Array(19,0,0,0,0);
 nHoliday[2021][8]   = new Array(0,0,0,0,0);
 nHoliday[2021][9]   = new Array(20,23,0,0,0);
 nHoliday[2021][10]  = new Array(12,0,0,0,0);
 nHoliday[2021][11]  = new Array(3,23,0,0,0);
 nHoliday[2021][12]  = new Array(23,0,0,0,0);

//xxx=new make_arr(4);
//for(i=1;i<=4;i++){xxx[i]="配列"+i;}
//for(i=1;i<=4;i++){document.write("xxx["+i+"]="+xxx[i]+" ");}
//function make_arr(x){
//this.length=x;
//for(i=1;i<=x;i++){this[i]="";}
//alert(this[i]);
//}

//for(q=2010; q<=2030 ;q++){
// for(p=1; p<=12 ;p++){
// nHoliday[q] = [p];
// alert(nHoliday[q]);
// }
//} 

 
