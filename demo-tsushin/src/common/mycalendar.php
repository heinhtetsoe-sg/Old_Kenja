<?php

require_once('for_php7.php');

// 2008/05/30 宮部作成
class mycalendar{

    //元号が変わった場合は以下の配列へ追加して下さい。
//         //2012/05/29 修正 明治は1868/9/8
//     var $def_nengo = array("明治","大正","昭和","平成");
//     var $nengoYear = array( 1868,  1912,  1926,  1989);
//     var $nengoMonth= array(    9,     7,    12,     1);
//     var $nengoDay  = array(    8,    30,    25,     8);
//     //年号識別記号
//     var $nengoID   = array(   "M",   "T",   "S",   "H");
//     //年号識別記号2 //20100514追加
//     var $nengoID2  = array(   "1",   "2",   "3",   "4");
    var $def_nengo = array();
    var $nengoYear = array();
    var $nengoMonth= array();
    var $nengoDay  = array();
    //年号識別記号
    var $nengoID   = array();
    //年号識別記号2
    var $nengoID2  = array();

    //コンストラクタ
    function mycalendar(){
    }

    //和暦リストを取得
    function getWareki() {
        if (get_count($this->def_nengo) <= 0) {
            $warekiList = common::getWarekiList();

            for ($i = 0; $i < get_count($warekiList); $i++) {
                $warekiInfo = $warekiList[$i];
                $startDate = array();
                $startDate = preg_split('/\//', $warekiInfo['Start']);

                $this->def_nengo[] = $warekiInfo['Name'];
                $this->nengoYear[] = intval($startDate[0]);
                $this->nengoMonth[] = intval($startDate[1]);
                $this->nengoDay[] = intval($startDate[2]);
                $this->nengoID[] = $warekiInfo['SName'];
                $this->nengoID2[] = $warekiInfo['CD'];
            }
        }
    }
    
    //元号に変換
    function toGengo($year, $month, $day){
        $this->getWareki();
        for($nengo=get_count($this->def_nengo);$nengo>0;$nengo--){
           if($this->nengoYear[$nengo-1]<$year)
               break;
           else if($this->nengoYear[$nengo-1]==$year){
               if($this->nengoMonth[$nengo-1]<$month)
                   break;
               else if($this->nengoMonth[$nengo-1]==$month){
                   if($this->nengoDay[$nengo-1]<=$day)
                       break;
               }
           }
        }
        return $nengo;
    }

 /*/   //西暦YYYY-MM-DDをGYY/MM/DDに変換
    function ChgWToJ($reqday){
        if (strlen($reqday)==0)
            return "";
        $number = preg_split("/-/", $reqday);
        switch(get_count($number)){
        case 1:
            $number[2]="1";
        case 2:
            $number[1]="1";
        }
        $gen = $this->toGengo($number[0], $number[1], $number[2]);
        if ($gen==0)
            return "";
       return sprintf("%s%02d/%02d/%02d", $this->nengoID[$gen-1]
                      ,$number[0]-$this->nengoYear[$gen-1]+1, $number[1], $number[2]);
    } */

   //西暦YYYY-MM-DDをGYY/MM/DDに変換
    function ChgWToJ($reqday){
        if (strlen($reqday)==0)
            return "";
        $number = preg_split("/-/", $reqday);
        $patrn = get_count($number);
        switch($patrn){
        case 1:
            $number[1]="1";
        case 2:
            $number[2]="1";
        }
        $gen = $this->toGengo($number[0], $number[1], $number[2]);
        if ($gen==0)
            return "";
        switch($patrn){
        case 1:
            return sprintf("%s%02d", $this->nengoID[$gen-1]
                      ,$number[0]-$this->nengoYear[$gen-1]+1);
        case 2:
            return sprintf("%s%02d/%02d", $this->nengoID[$gen-1]
                      ,$number[0]-$this->nengoYear[$gen-1]+1, $number[1]);
        default:
            return sprintf("%s%02d/%02d/%02d", $this->nengoID[$gen-1]
                      ,$number[0]-$this->nengoYear[$gen-1]+1, $number[1], $number[2]);
        }
    }
    
 /*/   //GYY/MM/DDを西暦YYYY-MM-DDに変換
    function ChgJToW($reqday){
        if (strlen($reqday)==0)
            return "";
        $number = preg_split("/\//", $reqday);
        switch(get_count($number)){
        case 1:
            $number[2]="1";
        case 2:
            $number[1]="1";
        }
        $id = substr($number[0], 0, 1);
        $y = substr($number[0], 1, 2);
        for($nengo=get_count($this->def_nengo);$nengo>0;$nengo--){
            if (strcmp($this->nengoID[$nengo-1], $id)==0)
                break;
        }
        if ($nengo==0)
            return "";
        return sprintf("%04d-%02d-%02d"
                      , $this->nengoYear[$nengo-1]+$y-1, $number[1], $number[2]);
    }
    
 */
 
 //GYY/MM/DDを西暦YYYY-MM-DDに変換
    function ChgJToW($reqday){
        $this->getWareki();
        if (strlen($reqday)==0)
            return "";
        $number = preg_split("/\//", $reqday);
        $patrn = get_count($number);
        switch($patrn){
        case 1:
            $number[1]="1";
        case 2:
            $number[2]="1";
        }
        $id = substr($number[0], 0, 1);
        $y = substr($number[0], 1, 2);
        for($nengo=get_count($this->def_nengo);$nengo>0;$nengo--){
            if (strcmp($this->nengoID[$nengo-1], $id)==0)
                break;
        }
        if ($nengo==0)
            return "";
        
        switch($patrn){
        case 1:
            return sprintf("%04d", $this->nengoYear[$nengo-1]+$y-1);
        case 2:
            return sprintf("%04d-%02d", $this->nengoYear[$nengo-1]+$y-1, $number[1]);
        default:
            return sprintf("%04d-%02d-%02d", $this->nengoYear[$nengo-1]+$y-1, $number[1], $number[2]);
        }
    }
 
    
   //西暦YYYY-MM-DDを元号YY年MM月DD日変換(YMDは半角,月日の十の位の0をスペースで出力) kurata(H21.3.10)
   function ChgWToJ_KANJI($reqday){
       $this->getWareki();
       if (strlen($reqday)==0)
            return "";
        $number = preg_split("/-/", $reqday);
        $patrn = get_count($number);

        switch($patrn){
        case 1:
            $number[1]="1";
        case 2:
            $number[2]="1";
        }
        $gen = $this->toGengo($number[0], $number[1], $number[2]);
        if ($gen==0)
            return "";
        switch($patrn){
        case 1:
            return sprintf("%s%2d年", $this->def_nengo[$gen-1]
                      ,$number[0]-$this->nengoYear[$gen-1]+1);
        case 2:
            return sprintf("%s%2d年%2d月", $this->def_nengo[$gen-1]
                      ,$number[0]-$this->nengoYear[$gen-1]+1, $number[1]);
        default:
            return sprintf("%s%2d年%2d月%2d日", $this->def_nengo[$gen-1]
                      ,$number[0]-$this->nengoYear[$gen-1]+1,$number[1], $number[2]);
        }
    }

    //西暦YYYY-MM-DDを元号YY/MM/DDに変換
    function ChgWToJK($reqday){
        $this->getWareki();
        if (strlen($reqday)==0)
            return "";
        $number = preg_split("/-/", $reqday);
        $patrn = get_count($number);
        switch($patrn){
        case 1:
            $number[1]="1";
        case 2:
            $number[2]="1";
        }
        $gen = $this->toGengo($number[0], $number[1], $number[2]);
        if ($gen==0)
            return "";
        switch($patrn){
        case 1:
            return sprintf("%s%02d", $this->def_nengo[$gen-1]
                      ,$number[0]-$this->nengoYear[$gen-1]+1);
        case 2:
            return sprintf("%s%02d/%02d", $this->def_nengo[$gen-1]
                      ,$number[0]-$this->nengoYear[$gen-1]+1, $number[1]);
        default:
            return sprintf("%s%02d/%02d/%02d", $this->def_nengo[$gen-1]
                      ,$number[0]-$this->nengoYear[$gen-1]+1, $number[1], $number[2]);
        }
    }

    //GYY/MM/DDを西暦YYYY-MM-DDに変換
    //function ChgJToW($reqday){
    //    if (strlen($reqday)==0)
    //        return "";
    //    $number = preg_split("/\//", $reqday);
    //    switch(get_count($number)){
    //    case 1:
    //        $number[2]="1";
    //    case 2:
    //        $number[1]="1";
    //    }
    //    $id = substr($number[0], 0, 1);
    //    $y = substr($number[0], 1, 2);
    //    for($nengo=get_count($this->def_nengo);$nengo>0;$nengo--){
    //        if (strcmp($this->nengoID[$nengo-1], $id)==0)
    //            break;
    //    }
    //    if ($nengo==0)
    //        return "";
    //    return sprintf("%04d-%02d-%02d"
    //                  , $this->nengoYear[$nengo-1]+$y-1, $number[1], $number[2]);
    // }
    
    //カレンダーコントロール作成
    function MyCalendarWin(&$form, $name, $value="", $extra = "")
    {
        //テキストエリア
        $initialdate = $this->ChgWToJ($value); //西暦YYYY-MM-DDをGYY/MM/DDに変換
        $form->ae( array("type"        => "text",
                         "name"        => $name,
                         "size"        => 12,
                         "maxlength"   => 12,
                         "extrahtml"   => $extra,
                         "value"       => $initialdate));
        /*/読込ボタンを作成する
        $form->ae( array("type"      => "button",
                         "name"      => "btn_mycalen",
                         "value"     => " ※ ",
                         "extrahtml" => "onclick=\"dayWin('" .REQUESTROOT ."/common/mycalendar.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}());\""));
        return $form->ge($name) .$form->ge("btn_mycalen");*/
        $img = "\n<A HREF=\"#\" ";
        $img .= "onClick=\"dayWin('" .REQUESTROOT ."/common/mycalendar.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}());\">";
        $img .= "<IMG SRC=\"".REQUESTROOT ."/image/calendar.gif\" style=\"border-style: none;\" ALIGN=top>";
        $img .= "</A>";
        return $form->ge($name) .$img;
    }
    //月コントロール作成
    function MyMonthWin(&$form, $name, $value="")
    {
        //--2009/11/02追加--テキストエリア
        if(substr($value,0,1)=='H' | substr($value,0,1)=='S'|
          substr($value,0,1)=='T' | substr($value,0,1)=='M' ){
            $initialdate = $value;
        }else{
            $initialdate = $this->ChgWToJ($value); //西暦YYYY-MM-DDをGYY/MM/DDに変換
        }
        //-----------
        global $sess;

        $form->ae( array("type"        => "text",
                         "name"        => $name,
                         "size"        => 12,
                         "maxlength"   => 12,
                         "extrahtml"   => "",
                         "value"       => $initialdate));
        /*/読込ボタンを作成する
        $form->ae( array("type"      => "button",
                         "name"      => "btn_mymonth",
                         "value"     => " 月 ",
                         "extrahtml" => "onclick=\"monthWin('" .REQUESTROOT ."/common/mymonth.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}());\""));
        return $form->ge($name) .$form->ge("btn_mymonth");*/
        $img = "\n<A ";
//        $img .= "onClick=\"monthWin('" .REQUESTROOT ."/common/mymonth.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}());\">";
        $img .= "onclick=\"loadwindow('" .REQUESTROOT ."/common/monthcalendar.php?name=$name&frame='+getFrameName(self) + '&date=' + document.forms[0]['$name'].value + '&CAL_SESSID=$sess->id&', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 200, 150)\">";
        $img .= "<IMG SRC=\"".REQUESTROOT ."/image/month.GIF\" style=\"border-style: none;\" ALIGN=top>";
        $img .= "</A>";

        return View::setIframeJs() .$form->ge($name) .$img;
    }


//---20090824 kurata-----------------------------------------------------------------//
    //カレンダーコントロール作成(加工版)
    function MyCalendarWin2(&$form, $name, $value="", $extra = "")
    {
      /*/kurata 20090828    
        (変更点)
         ①引数($value)で複数の文字形式の入力を受け付けるようにした点（出力は同じ）
          （桁数と使用している"-"と"/"の数で判断） 
            種類) 1) "2009-01-01"(※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  2) "2009/01/01"(※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  3) "20090101"  (※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  4) "H21-01-01" (※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  5) "H21/01/01" (※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  6) "H210101"   (※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  7) それ以外（→今日の日付に変換）
         ②javascript($extra)を動作できるようにした点($extraで記述するfunctionは呼び出し元プログラムのjsに記述してください.もちろん一般のも使えますが)
             例) (呼び出し元) $extra = " btn_submit('edit') ; "
       */
     
    //文字列に変換（一律)
       $value = "$value";
    //テキストエリア //①自動変換部 ↓kurata 20090820 ココから(空欄は②から）
    if($value != "")
    {
          //文字の長さ
            $length = strlen($value);
          //文字に特定文字が含まれるか
            $split1  = substr_count($value, "-" );//2009-01-01タイプ 
            $split2  = substr_count($value, "/"); //H21/01/01タイプ
         //場合わけ
          if($split1 == 2)     {if($length == 9) {$type = 4;}           //H21-01-01タイプ
                           else if($length == 10){$type = 1;}         //2009-01-01タイプ 
                           else                  {$type = 7;}}                                                 //その他
     else if($split2 == 2)     {if($length == 9) {$type = 5;}           //H21/01/01タイプ
                           else if($length == 10){$type = 2;}         //2009/01/01タイプ
                           else                  {$type = 7;}}                                                 //その他
     else if($split1 == "" && $split2 == "")
                               {if($length == 7){$type = 6;}           //H210101タイプ
                           else if($length == 8){$type = 3;}           //20090101タイプ    
                           else                 {$type = 7;}}                                                 //その他
     else {$type = 7 ;}                                                 //その他
          //それぞれ処理
          switch($type){
           case 1 ://2009-01-01タイプ 
              $initialdate = $this->ChgWToJ($value);
              break;
           case 2 ://2009/01/01タイプ
              $henkan = str_replace("/","-",$value);
              $initialdate = $this->ChgWToJ($henkan);
              break;
           case 3 ://20090101タイプ 
              $henkan = substr($value,0,4)."-".substr($value,4,2)."-".substr($value,6,2);
              $initialdate = $this->ChgWToJ($henkan); //西暦YYYY-MM-DDをGYY/MM/DDに変換
              break;
           case 4 ://H21-01-01タイプ
              $initialdate = str_replace("-","/",$value);
              break;
           case 5 ://H21/01/01タイプ
              $initialdate = $value;
              break;
           case 6 ://H210101タイプ
              $initialdate = substr($value,0,3)."-".substr($value,3,2)."-".substr($value,5,2);
              break;
           case 7 ://その他は現在値を代入
              
               $errorflg=2;
               $initialdate="";
              break;
           }   
          
          //入力時の日付の存在チェック
          if($errorflg != 2){
          $errorflg = $this->chkYmd($initialdate);
          }
       //エラーの出力  
         if($errorflg==2){
             //形式が正しくない（chkYmd前で引っかかる）
              echo"<script language=\"JavaScript\">";
              echo"alert(\"　指定された日付【".$value."】\\nは正しい日付形式でないため空欄を入力します。\");";
              echo"</script>";
             $initialdate="";
          }else if($errorflg==1){
             //形式は正しいが日にちが存在しない（chkYmdで引っかかる）
             echo"<script language=\"JavaScript\">";
              echo"alert(\"　指定された日付【".$value."】\\nは存在しない日付です。空欄を入力します。\");";
              echo"</script>";
          $initialdate=""; //error出して""で入力する
          }
     }  
      //②後半部      
          //テキストフィールドに手入力操作させないためフォーカスさせない(20090821 kurata)  
          $command = "onFocus = \"this.blur();\"  ";  
          
          $form->ae( array("type"        => "text",
                           "name"        => $name,
                           "size"        => 12,
                           "maxlength"   => 12,
                           "extrahtml"   => $command , //20090820 kurata (元々extraがはいってましたが機能しないため除去しフォーカスさせない機能を追加)
                           "value"       => $initialdate));
        //読込ボタンを作成する
        //$form->ae( array("type"      => "button",
        //                 "name"      => "btn_mycalen",
        //                 "value"     => " ※ ",
        //                 "extrahtml" => "onclick=\"dayWin_kura('" .REQUESTROOT ."/common/mycalendar.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}()); return btn_submit('edit') ; \""));
        //                 //"extrahtml" => "onclick= \"alert(); \""));
        //return $form->ge($name) .$form->ge("btn_mycalen");
        
       //マイカレボタンの作成(2009/08/20 daywin直後にjavascriptを呼べるように改変しました kurata)　　 
        $img = "\n<A HREF=\"# \" ";
        $img .= "onClick=\"dayWin('" .REQUESTROOT ."/common/mycalendar.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}());   ";
        $img .= " return $extra   "; //20090930 KURATA
        $img .= " \"> ";                
        $img .= "<IMG SRC=\"".REQUESTROOT ."/image/calendar.gif\" style=\"border-style: none;\" ALIGN=top>";
        $img .= "</A>";
        
        
        
        return $form->ge($name) .$img;
    }
    //月コントロール作成(加工版)
    function MyMonthWin2(&$form, $name, $value = "", $extra = "" )
    {
      /*/kurata 20090824    
        (変更点)
         ①引数($value)で複数の文字形式の入力を受け付けるようにした点(出力は同じ）
          （桁数と使用している"-"と"/"の数で判断） 
            種類) 1) "2009-01"(※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  2) "2009/01"(※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  3) "200901" (※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  4) "H21-01" (※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  5) "H21/01" (※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  6) "H2101"  (※日付チェックはphpで定型的なやり方で導入済。見つからないと空欄)
                  7) それ以外（→今日の日付に変換）
         ②javascript($extra)を動作できるようにした点($extraで記述するfunctionは呼び出し元プログラムのjsに記述してください.もちろん一般のも使えますが)
             記述例) (呼び出し元) $extra = " btn_submit('edit') ; "
       */
         
   //テキストエリア //①自動変換部 (空欄は②から）
   if($value != "")
   {
          //文字の長さ
            $length = strlen($value);
          //文字に特定文字が含まれるか
            $split1  = substr_count($value, "-" );//2009-01,H21-01タイプ 
            $split2  = substr_count($value, "/"); //2009/01,H21/01タイプ
          //場合わけ
          if($split1 == 1)     {if($length == 7){$type = 1;}  //2009-01タイプ
                           else if($length == 6){$type = 4;}  //H21-01タイプ
                           else                 {$type = 7;}}                                                 //その他
     else if($split2 == 1)     {if($length == 7){$type = 2;}  //2009/01タイプ
                           else if($length == 6){$type = 5;}  //H21/01タイプ
                           else                 {$type = 7;}}                                                 //その他
     else if($split1 == "" && $split2 == "")
                               {if($length == 6){$type = 3;}  //200901タイプ
                           else if($length == 5){$type = 6;} //H2101タイプ    
                           else                 {$type = 7;}}                                                 //その他
     else { $type = 7 ;}                                       //その他
          
         //それぞれ処理
          switch($type){
           case 1 ://2009-01タイプ
              $initialdate = $this->ChgWToJ($value);
              break;
           case 2 ://2009/01タイプ
              $henkan = str_replace("/","-",$value);
              $initialdate = $this->ChgWToJ($henkan);
              break;
           case 3 ://200901タイプ
              $henkan = substr($value,0,4)."-".substr($value,4,2);
              $initialdate = $this->ChgWToJ($henkan); //西暦YYYY-MMをGYY/MMに変換
              break;
           case 4 ://H21-01タイプ
              $initialdate = str_replace("-","/",$value);
              break;
           case 5 ://H21/01タイプ
              $initialdate = $value;
              break;
           case 6 ://H2101タイプ
              $initialdate = substr($value,0,3)."/".substr($value,3,2);
              break;
           case 7 ://それ以外のタイプ
             
              $errorflg==2; // 1はchkYmで使うため
              $initialdate="";
              break;
           }   

          //入力時の日付の存在チェック
         if($errorflg != 2){
            $errorflg = $this->chkYm($initialdate);
          }
          
        //エラーチェック  
          if($errorflg==2){
             //形式が正しくない（chkYm前で引っかかる）
              echo"<script language=\"JavaScript\">";
              echo"alert(\"　指定された年月【".$value."】\\nは正しい日付形式でないため空欄を入力します。\");";
              echo"</script>";
             $initialdate="";
          }else if($errorflg==1){
             //形式は正しいが日付が存在しない(chkYmで引っかかる）
              echo"<script language=\"JavaScript\">";
              echo"alert(\"　指定された年月【".$value."】\\nは存在しない年月です。空欄を入力します。\");";
              echo"</script>";
             $initialdate=""; //error出して""で入力する
          }
    }  
     //②後半部
       //テキストフィールドに手入力操作させないためフォーカスさせない(20090821 kurata)  
        $command = "onFocus = \"this.blur();\" ";  
       //textフィールド部
        $form->ae( array("type"        => "text",
                         "name"        => $name,
                         "size"        => 12,
                         "maxlength"   => 12,
                         "extrahtml"   => $command ,
                         "value"       => $initialdate));
       //読込ボタン部
      
        /*/ $form->ae( array("type"      => "button",
                        "name"      => "btn_mymonth",
                         "value"     => " 月 ",
                         "extrahtml" => "onclick=\"monthWin('" .REQUESTROOT ."/common/mymonth.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}());\""));
        return $form->ge($name) .$form->ge("btn_mymonth"); */
        global $sess;
        // コールバック関数パラメタの文字エスケープ
        $extra = str_replace("'", "\\'", $extra);
        $img = "\n<A  ";
//         $img .= "onClick=\"monthWin('" .REQUESTROOT ."/common/mymonth.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}());  ";
        $img .= "onclick=\"loadwindow('" .REQUESTROOT ."/common/monthcalendar.php?name=$name&frame='+getFrameName(self) + '&date=' + document.forms[0]['$name'].value + '&extra=".$extra."&CAL_SESSID=$sess->id&', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 200, 150); ";
//         $img .= " return $extra  "; //20090824 KURATA ///&EXTRA='+$extra+'
        $img .= " \"> ";                
        $img .= "<IMG SRC=\"".REQUESTROOT ."/image/month.GIF\" style=\"border-style: none;\" ALIGN=top>";
        $img .= "</A>";
        return View::setIframeJs() .$form->ge($name) .$img;
    }



// 年月日存在チェック
 function chkYmd($value)
 {  
/*/kurata 20090825    
         ①引数($value)で複数の文字形式の入力を受け付けが可能（出力は同じ）
          （桁数と使用している"-"と"/"の数で判断） 
            受けつけ可能な種類) 1) "2009-01-01"
                                2) "2009/01/01"
                                3) "20090101"  
                                4) "H21-01-01" 
                                5) "H21/01/01" 
                                6) "H210101"   
         ②返し値は$errorflgで0(存在確認),1(存在しない)を出力
       */
        //テキストエリア //①自動変換部 ↓kurata 20090820 ココから
          if($value != ""){
          //文字の長さ
            $length = strlen($value);
          //文字に特定文字が含まれるか
            $split1  = substr_count($value, "-" );//2009-01-01タイプ 
            $split2  = substr_count($value, "/"); //H21/01/01タイプ
          //場合わけ
          if($split1 == 2)     {if($length == 9) {$type = 4;}    //H21-01-01タイプ
                           else if($length == 10){$type = 1;}   //2009-01-01タイプ 
                           else                  {$type = 7;}}                                                 //その他
     else if($split2 == 2)     {if($length == 9) {$type = 5;}    //H21/01/01タイプ
                           else if($length == 10){$type = 2;}   //2009/01/01タイプ
                           else                  {$type = 7;}}                                                 //その他
     else if($split1 == "" && $split2 == "" )
                               {if($length == 7) {$type = 6;}    //H210101タイプ
                           else if($length == 8) {$type = 3;}   //20090101タイプ    
                           else                  {$type = 7;}}                                                 //その他
     else  {  $type = 7 ; }                                        //その他
          
          //それぞれ処理(xxxx-xx-xxに統一する）
          switch($type){
           case 1 ://2009-01-01タイプ 
              $initialdate = $value;
              break;
           case 2 ://2009/01/01タイプ
              $initialdate = str_replace("/","-",$value);
              break;
           case 3 ://20090101タイプ 
              $initialdate = substr($value,0,4)."-".substr($value,4,2)."-".substr($value,6,2);
              break;
           case 4 ://H21-01-01タイプ
              $henkan = str_replace("-","/",$value);
              $initialdate = $this->ChgJToW($henkan);
              break;
           case 5 ://H21/01/01タイプ
              $initialdate = $this->ChgJToW($value);
              break;
           case 6 ://H210101タイプ
              $henkan = substr($value,0,3)."/".substr($value,3,2)."/".substr($value,5,2);
              $initialdate = $this->ChgJToW($henkan);
              break;
           case 7 ://その他はエラー(1)を出力
              $errorflg = 1 ;
              break;
           }   
          } // ↑kurata 20090820 マデ
       
        if(!empty($initialdate))
        {
         list($yy,$mm,$dd) = explode("-", $initialdate);
         
         if(checkdate($mm,$dd,$yy))
         {     $errorflg = 0 ; }//存在する時
         else
         {     $errorflg = 1 ; }   
        
        }else{ $errorflg = 1 ; }
  
     return $errorflg;   
 }


// 年月存在チェック
 function chkYm($value)
 {
       /*/kurata 20090825    
         ①引数($day)で複数の文字形式の入力を受け付けることが可能(出力は同じ）
          （桁数と使用している"-"と"/"の数で判断） 
            受付可能な種類) 1) "2009-01"
                            2) "2009/01"
                            3) "200901" 
                            4) "H21-01" 
                            5) "H21/01" 
                            6) "H2101"  
         ②返し値は$errorflgで0(存在確認),1(存在しない)を出力
       */
        //テキストエリア //①自動変換部 ↓kurata 20090824 ココから
          if($value != ""){
          //文字の長さ
            $length = strlen($value);
          //文字に特定文字が含まれるか
            $split1  = substr_count($value, "-" );//2009-01,H21-01タイプ 
            $split2  = substr_count($value, "/"); //2009/01,H21/01タイプ
          //場合わけ
          if($split1 == 1)     {if($length == 7){$type = 1;}  //2009-01タイプ
                           else if($length == 6){$type = 4;} //H21-01タイプ
                           else                 {$type = 7;}}                                                 //その他
     else if($split2 == 1)     {if($length == 7){$type = 2;}  //2009/01タイプ
                           else if($length == 6){$type = 5;} //H21/01タイプ
                           else                 {$type = 7;}}                                                 //その他
     else if($split1 == "" && $split2 == "")
                               {if($length == 6){$type = 3;}  //200901タイプ
                           else if($length == 5){$type = 6;} //H2101タイプ    
                           else                 {$type = 7;}}                                                 //その他
     else { $type = 7 ;}                                       //その他
          
         //それぞれ処理(xxxx-xxに統一する）
          switch($type){
           case 1 ://2009-01タイプ
              $initialdate = $value;
              break;
           case 2 ://2009/01タイプ
              $initialdate = str_replace("/","-",$value);
              break;
           case 3 ://200901タイプ
              $initialdate = substr($value,0,4)."-".substr($value,4,2);
              break;
           case 4 ://H21-01タイプ
              $henkan = str_replace("-","/",$value);
              $initialdate = $this->ChgJToW($henkan);
              break;
           case 5 ://H21/01タイプ
              $initialdate = $this->ChgJToW($value);
              break;
           case 6 ://H2101タイプ
              $henkan = substr($value,0,3)."/".substr($value,3,2);
              $initialdate = $this->ChgJToW($henkan);
              break;
           case 7 ://それ以外のタイプ（現在月で指定）
              $errorflg = 1 ;
              break;
              
           }   
          } // ↑kurata 20090824 マデ

        if(!empty($initialdate))
        {
         list($yy,$mm) = explode("-", $initialdate);
         $dd = "01" ;//判断に必要なため
         
         if(checkdate($mm,$dd,$yy))
         {     $errorflg = 0 ; }//存在する時
         else
         {     $errorflg = 1 ; }   
        
        }else{ $errorflg = 1 ; }
  
     return $errorflg;   
 }

   //kurata　
   //西暦YYYY-MM-DDを元号YY年MM月DD日変換(YMDは半角,月日の十の位の0をスペースで出力) kurata(H21.3.10)
   //$NendFlgは　1→"年度"(1～３月は1年引く)で出力。　その他(0他)→通常の"年"で出力
   //ex)(2008-03-01,1)→平成20年度 3月 1日、(2008-03-01,0)→平成21年 3月 1日
   //   (2008,1)→平成21年度(1は機能しない)
   function ChgWToJ_KANJI2($reqday,$NendFlg,$HenkanFlg){
        if (strlen($reqday)==0)
            return "";
        $number = preg_split("/-/", $reqday);
        $patrn = get_count($number);

        switch($patrn){
        case 1:
            $number[1]="1";
        case 2:
            $number[2]="1";
        }
        $gen = $this->toGengo($number[0], $number[1], $number[2]);
        if ($gen==0)
            return "";
         //NendFlgが1（"年度"出力)でpatrnが1でない(年度のみの出力でない)ときは調整する 
          //”月”が １月～３月のときは[前年]の"<年度>  
        if ($patrn != 1 && $NendFlg == 1){
            $g = $number[1];
            if(($g == '01')||($g == '02')||($g == '03')){          
            $number[0] = $number[0] - 1;
           } }
        $number[0]=$number[0]-$this->nengoYear[$gen-1]+1; //先に実行する
        //HenkanFlgが1（"全角"）のときは$numberを全角化する
        if($HenkanFlg == 1)
           {$number =$this->HenkanFunc($number,$HenkanFlg);}
        switch($patrn){
        case 1:
           if($NendFlg == 1){
                switch($HenkanFlg){
                  case 1:
                    return sprintf("%s%s年度", $this->def_nengo[$gen-1]
                      ,$number[0]);
                default:
                    return sprintf("%s%2d年度", $this->def_nengo[$gen-1]
                      ,$number[0]);
                }      
            }else{
                switch($HenkanFlg){
                  case 1:
                    return sprintf("%s%s年", $this->def_nengo[$gen-1]
                      ,$number[0]);
                  default:
                    return sprintf("%s%2d年", $this->def_nengo[$gen-1]
                      ,$number[0]);
                 }     
            }          
        case 2:
           if($NendFlg == 1){
                  switch($HenkanFlg){
                  case 1:
                    return sprintf("%s%s年度%s月", $this->def_nengo[$gen-1]
                      ,$number[0],$number[1]);
                  default:
                    return sprintf("%s%2d年度 %2d月", $this->def_nengo[$gen-1]
                      ,$number[0], $number[1]);
                 }     
           }else{
                switch($HenkanFlg){
                  case 1:
                    return sprintf("%s%s年%s月", $this->def_nengo[$gen-1]
                      ,$number[0],$number[1]);
                  default:
                    return sprintf("%s%2d年%2d月", $this->def_nengo[$gen-1]
                      ,$number[0], $number[1]);
                  }    
           }           
        default:
        
           if($NendFlg == 1){
                switch($HenkanFlg){
                  case 1:
                    return sprintf("%s%s年度%s月%s日", $this->def_nengo[$gen-1]
                          ,$number[0],$number[1], $number[2]);
                  default:
                    return sprintf("%s%2d年度%2d月%2d日", $this->def_nengo[$gen-1]
                          ,$number[0],$number[1], $number[2]);
                  }        
           }else{
                switch($HenkanFlg){
                  case 1:
                    return sprintf("%s%s年%s月%s日", $this->def_nengo[$gen-1]
                          ,$number[0],$number[1], $number[2]);
                  default:
                    return sprintf("%s%2d年%2d月%2d日", $this->def_nengo[$gen-1]
                          ,$number[0],$number[1], $number[2]);
                 }         
          
           }
        }
    }

       function HenkanFunc($number,$HenkanFlg)
       { 
        if ($HenkanFlg == 1){
         $number[0] = substr_replace($number[0], " ", 0, -2);
         $number[1] = substr_replace($number[1], " ", 0, -2);
         $number[2] = substr_replace($number[2], " ", 0, -2);
         }
         //mb_internal_encoding("UTF-8");
         $number[0]= mb_convert_kana($number[0],"A","UTF-8");
         $number[1]= mb_convert_kana($number[1],"A","UTF-8");
         $number[2]= mb_convert_kana($number[2],"A","UTF-8");
         return $number;
       }

//###############################################################################
//20100519
    //カレンダーコントロール作成
    function MyCalendarWin3(&$form, $name, $value="", $extra = "", $reload = "", $extraClick = "")
    {
        
        $span1 = "<span nowrap onMouseOver=\"AcceptnoMousein(event, '例）昭和50年10月10日　⇒　3501010　<br><br>例）平成5年7月8日　　　⇒　4050708　<br><br>（明治：1　大正：2　昭和：3　平成：4）')\" onMouseOut=\"AcceptnoMouseout()\">";
        $span2 = "</span>";
        
        //テキストエリア
        $initialdate = $this->ChgWToN($value); //西暦YYYY-MM-DDをGYY/MM/DDに変換
        //エクストラ
        $command  = "style=\"ime-mode:inactive\"; ";
        
        //$command .= "onKeyUp = \"key7AndCheck(this); \" ";
        if($extra != ""){
            $command .= " $extra ; ";  
        }else{
            $command .= "onBlur = \"key7AndCheck(this);\" ";
        }
        $form->ae( array("type"        => "text",
                         "name"        => $name,
                         "size"        => 7,
                         "maxlength"   => 7,
                         "extrahtml"   => $command,
                         "value"       => $initialdate));
        /*/読込ボタンを作成する
        $form->ae( array("type"      => "button",
                         "name"      => "btn_mycalen",
                         "value"     => " ※ ",
                         "extrahtml" => "onclick=\"dayWin('" .REQUESTROOT ."/common/mycalendar.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}());\""));
        return $form->ge($name) .$form->ge("btn_mycalen");*/
        $img = "\n<A HREF=\"#\" ";
        if($extra != " readonly" && $extraClick == ""){
           $img .= "onClick=\"dayWin2('" .REQUESTROOT ."/common/mycalendar2.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), '$reload');\">";
        } else if($extraClick != ""){
           $img .= "onClick=\"dayWinExtra('" .REQUESTROOT ."/common/mycalendar2.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), '$extraClick');\">";
        }
        $img .= "<IMG SRC=\"".REQUESTROOT ."/image/calendar.gif\" style=\"border-style: none;\" ALIGN=top>";
        $img .= "</A>";
        return $span1.$form->ge($name) .$img.$span2;
    }
//###############################################################################
//20100519
//月コントロール作成
    function MyMonthWin3(&$form, $name, $value="", $extra="", $reload = "")
    {
        
        $span1 = "<span nowrap onMouseOver=\"AcceptnoMousein(event, '例）昭和50年10月　⇒　35010　<br><br>例）平成5年7月　　　⇒　40507　<br><br>（明治：1　大正：2　昭和：3　平成：4）')\" onMouseOut=\"AcceptnoMouseout()\">";
        $span2 = "</span>";
        
        //--2009/11/02追加--テキストエリア
        //if(substr($value,0,1)=='H' | substr($value,0,1)=='S'|
        //  substr($value,0,1)=='T' | substr($value,0,1)=='M' ){
        //    $initialdate = $value;
        //}else{
        //    $initialdate = $this->ChgWToJ($value); //西暦YYYY-MM-DDをGYY/MM/DDに変換
        //}
        //-----------
        //2010/05/19
        $initialdate = $this->ChgWToN($value); //西暦YYYY-MM-DDをGYY/MM/DDに変換
        //エクストラ
        $command  = "style=\"ime-mode:inactive\"; ";
        $command .= "onBlur = \"key5AndCheck(this); \" ";
        if($extra != ""){
            $command .= " return $extra ; ";  
        }
        $form->ae( array("type"        => "text",
                         "name"        => $name,
                         "size"        => 5,
                         "maxlength"   => 5,
                         "extrahtml"   => $command,
                         "value"       => $initialdate));
        /*/読込ボタンを作成する
        $form->ae( array("type"      => "button",
                         "name"      => "btn_mymonth",
                         "value"     => " 月 ",
                         "extrahtml" => "onclick=\"monthWin('" .REQUESTROOT ."/common/mymonth.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}());\""));
        return $form->ge($name) .$form->ge("btn_mymonth");*/
        $img = "\n<A HREF=\"#\" ";
        $img .= "onClick=\"monthWin('" .REQUESTROOT ."/common/mymonth2.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), '$reload');\">";
        $img .= "<IMG SRC=\"".REQUESTROOT ."/image/month.GIF\" style=\"border-style: none;\" ALIGN=top>";
        $img .= "</A>";
        return $span1.$form->ge($name) .$img.$span2;
    }
//###############################################################################
//20101130
//月コントロール作成
    function MyMonthWin4(&$form, $name, $value="", $extra="",$extra2="")
    {
        //--2009/11/02追加--テキストエリア
        //if(substr($value,0,1)=='H' | substr($value,0,1)=='S'|
        //  substr($value,0,1)=='T' | substr($value,0,1)=='M' ){
        //    $initialdate = $value;
        //}else{
        //    $initialdate = $this->ChgWToJ($value); //西暦YYYY-MM-DDをGYY/MM/DDに変換
        //}
        //-----------
        //2010/05/19
        $initialdate = $this->ChgWToN($value); //西暦YYYY-MM-DDをGYY/MM/DDに変換
        //エクストラ
        $command  = "style=\"ime-mode:inactive\"; ";
        $command .= "onBlur = \"key5AndCheck(this); \" ";
        if($extra != ""){
            $command .= " return $extra ; ";  
        }
        $form->ae( array("type"        => "text",
                         "name"        => $name,
                         "size"        => 5,
                         "maxlength"   => 5,
                         "extrahtml"   => $command,
                         "value"       => $initialdate));
        /*/読込ボタンを作成する
        $form->ae( array("type"      => "button",
                         "name"      => "btn_mymonth",
                         "value"     => " 月 ",
                         "extrahtml" => "onclick=\"monthWin('" .REQUESTROOT ."/common/mymonth.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}());\""));
        return $form->ge($name) .$form->ge("btn_mymonth");*/
        $img = "\n<A HREF=\"#\" ";
        $img .= "onClick=\"monthWin('" .REQUESTROOT ."/common/mymonth2.html', '$name', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}());  ";
        $img .= " return $extra2  "; //20090824 KURATA
        $img .= " \"> ";                
        $img .= "<IMG SRC=\"".REQUESTROOT ."/image/month.GIF\" style=\"border-style: none;\" ALIGN=top>";
        $img .= "</A>";
        return $form->ge($name) .$img;
    }

//////////////////////////////////////////////////////////////////////
   //西暦YYYY-MM-DDをNYYMMDDに変換
    function ChgWToN($reqday){
        if (strlen($reqday)==0)
            return "";
        $number = preg_split("/-/", $reqday);
        $patrn = get_count($number);
        switch($patrn){
        case 1:
            $number[1]="1";
        case 2:
            $number[2]="1";
        }
        $gen = $this->toGengo($number[0], $number[1], $number[2]);
        if ($gen==0)
            return "";
        switch($patrn){
        case 1:
            return sprintf("%d%02d", $this->nengoID2[(int)$gen-1]
                      ,(int)$number[0]-(int)$this->nengoYear[(int)$gen-1]+1);
        case 2:
            return sprintf("%d%02d%02d", $this->nengoID2[(int)$gen-1]
                      ,(int)$number[0]-(int)$this->nengoYear[(int)$gen-1]+1, $number[1]);
        default:
            return sprintf("%d%02d%02d%02d", $this->nengoID2[(int)$gen-1]
                      ,(int)$number[0]-(int)$this->nengoYear[(int)$gen-1]+1, $number[1], $number[2]);
        }
    }




}
/*/
//-----------------------------------------------------------------------//
//Javascript部                                                           // 
//-----------------------------------------------------------------------//
?>
    <script langage="JavaScript" type="text/javascript">
    //カレンダー子ウインドゥの呼出し
    function dayWin(URL, IN, x, y){
        var cmdstr="document.forms[0]."+IN+".value";
        var reqday=eval(cmdstr);
        var strObj=reqday;
        var para="";
        var browser = navigator.userAgent;
        //alert(browser);
        if ( browser.match( /MSIE[^0-9a-zA-Z]*([0-9.]+[^;])/i ) ) {
            if (RegExp.$1>=7) {
                //alert("ie7以上");
                para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:200px; dialogHeight:210px; location:0; directories:0; status:0;";
            }
            else {
                //alert("ie6以下");
                para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:250px; dialogHeight:260px; location:0; directories:0; status:0;";
            }
        }
        else {
            alert("ブラウザはIEのみのサポートです。");
            return FALSE;
        }
        var yourDate=showModalDialog(URL,strObj,para);
        if (yourDate!=""){
            cmdstr="document.forms[0]."+IN+".value=yourDate";
            reqday=eval(cmdstr);
        }
    }
    function monthWin(URL, IN, x, y){
        var cmdstr="document.forms[0]."+IN+".value";
        var reqday=eval(cmdstr);
        var strObj=reqday;
        var para="";
        var browser = navigator.userAgent;
        //alert(browser);
        if ( browser.match( /MSIE[^0-9a-zA-Z]*([0-9.]+[^;])/i ) ) {
            if (RegExp.$1>=7) {
                //alert("ie7以上");
                para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:180px; dialogHeight:180px; location:0; directories:0; status:0;";
            }
            else {
                //alert("ie6以下");
                para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:220px; dialogHeight:200px; location:0; directories:0; status:0;";
            }
        }
        else {
            alert("ブラウザはIEのみのサポートです。");
            return FALSE;
        }
        var para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:220px; dialogHeight:200px; location:0; directories:0; status:0;";
        var yourMonth=showModalDialog(URL,strObj,para);
        if (yourMonth!=""){
            cmdstr="document.forms[0]."+IN+".value=yourMonth";
            reqday=eval(cmdstr);
        }
           //return FALSE;//20090824 kurata(あると動かないので…)
    }

//--20090824 kurata-------------------------------------------------------------------------//
    //カレンダー子ウインドゥの呼出し(加工版）
    function dayWin2(URL, IN, x, y){
        var cmdstr="document.forms[0]."+IN+".value";
        var reqday=eval(cmdstr);
        var strObj=reqday;
        var para="";
        var browser = navigator.userAgent;
        //alert(browser);
        if ( browser.match( /MSIE[^0-9a-zA-Z]*([0-9.]+[^;])/i ) ) {
            if (RegExp.$1>=7) {
                //alert("ie7以上");
                para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:200px; dialogHeight:210px; location:0; directories:0; status:0;";
            }
            else {
                //alert("ie6以下");
                para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:250px; dialogHeight:260px; location:0; directories:0; status:0;";
            }
        }
        else {
            alert("ブラウザはIEのみのサポートです。");
            return FALSE;
        }
        var yourDate=showModalDialog(URL,strObj,para);
        if (yourDate!=""){
            cmdstr="document.forms[0]."+IN+".value=yourDate";
            reqday=eval(cmdstr);
        }
    }
    //カレンダー子ウインドゥの呼出し(加工版）
    function monthWin2(URL, IN, x, y)
    {
        var cmdstr="document.forms[0]."+IN+".value";
        var reqday=eval(cmdstr);
        var strObj=reqday;
        var para="";
        var browser = navigator.userAgent;
        //alert(browser);
        if ( browser.match( /MSIE[^0-9a-zA-Z]*([0-9.]+[^;])/i ) ) {
            if (RegExp.$1>=7) {
                //alert("ie7以上");
                para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:180px; dialogHeight:180px; location:0; directories:0; status:0;";
            }
            else {
                //alert("ie6以下");
                para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:220px; dialogHeight:200px; location:0; directories:0; status:0;";
            }
        }
        else {
            alert("ブラウザはIEのみのサポートです。");
            return FALSE;
        }
        var para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:220px; dialogHeight:200px; location:0; directories:0; status:0;";
        
        var yourMonth=showModalDialog(URL,strObj,para);
        if (yourMonth!=""){
            cmdstr="document.forms[0]."+IN+".value=yourMonth";
            reqday=eval(cmdstr);
        }
           //return FALSE;//20090824 kurata(あると動かないので…)

    }
    
    
    
    </script>
*/  
