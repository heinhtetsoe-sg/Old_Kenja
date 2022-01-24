<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja131bModel extends Model {
    var $field      = array();
    var $select_data    = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knja131bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //処理年月日
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力選択  //NO001
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "seito"            =>  VARS::post("seito"),            //帳票種別（生徒指導要録）
                             "simei"            =>  VARS::post("simei"),            //帳票種別（生徒・保護者氏名出力）
                             "inei"             =>  VARS::post("inei"),             //帳票種別（印影出力）
                             "katsudo"          =>  VARS::post("katsudo"),          //帳票種別（活動の記録）
                             "gakushu1"         =>  VARS::post("gakushu1"),         //帳票種別（学習の記録・前期課程）
                             "gakushu2"         =>  VARS::post("gakushu2"),         //帳票種別（学習の記録・後期課程）
                             "tani"             =>  VARS::post("tani"),             //帳票種別（修得単位の記録）
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();

        //初期値の設定
        $this->Properties["useSchregRegdHdat"] = $this->Properties["useSchregRegdHdat"] ? $this->Properties["useSchregRegdHdat"] : 0;
    }


    function getDownloadModel()
    {
        //CSVデータ抽出処理
        //DB接続
        $db = Query::dbCheckOut();

        $contents = ""; //CSVデータ出力用
        $data = false;

        //学籍番号ループ
        for($i=0;$i<get_count($this->select_data["selectdata"]);$i++){
            if( $this->field["seito"] == "1" ) $contents .= $this->getResult1($db,$i);  //１．生徒指導要録
            if( $this->field["tani"] == "1" ) $contents .= $this->getResult2($db,$i);   //２．修得単位の記録
            if( $this->field["gakushu"] == "1" ) $contents .= $this->getResult3($db,$i);//３．学習の記録
            if( $this->field["katsudo"] == "1" ) $contents .= $this->getResult4($db,$i);//４．活動の記録
            if($contents!="") $data = true;
        }//学籍番号ループ

        //DB切断
        Query::dbCheckIn($db);

        if ($data){
            $couse="生徒指導要録";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }//getDownloadModelの綴り


    //１．生徒指導要録
    function getResult1($db,$i)
    {
            // 生徒住所履歴 //履歴を降順に読み込み、最大３件まで出力
            $query = knja131bQuery::getinfo_ps3_1($this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            $s_count = 0;   //出力件数
            $s_zip_new = $s_zip_old1 = $s_zip_old2 = "";
            $s_addr1_new = $s_addr1_old1 = $s_addr1_old2 = "　";
            $s_addr2_new = $s_addr2_old1 = $s_addr2_old2 = "　";
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);
                //現住所（生徒）
                if($s_count == 0){
                    $s_zip_new = $row["ZIPCD"];
                    $s_addr1_new = $row["ADDR1"];
                    $s_addr2_new = $row["ADDR2"];
                }
                //現住所（生徒）（前）
                if($s_count == 1){
                    $s_zip_old1 = $row["ZIPCD"];
                    $s_addr1_old1 = $row["ADDR1"];
                    $s_addr2_old1 = $row["ADDR2"];
                }
                //現住所（生徒）（前前）
                if($s_count == 2){
                    $s_zip_old2 = $row["ZIPCD"];
                    $s_addr1_old2 = $row["ADDR1"];
                    $s_addr2_old2 = $row["ADDR2"];
                }

                if($s_count == 2)   break;
                $s_count++;

                $data = true;
            }

            // 生徒異動履歴 //
            $query = knja131bQuery::getinfo_ps4_1($this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            $ia = 0;    //休学・留学回数
            $enterdate1 = $enterdate2 = $enterreason2 = $transferdate1 = $transferdate2 = $transferreason2 = "　";
            $transferdate4 = $transferplace4 = "　";
            $transferdate3_1 = $transferreason3_1 = $transferplace3_1 = "　";
            $transferdate3_2 = $transferreason3_2 = $transferplace3_2 = "　";
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                $namecd1 = $row["NAMECD1"];
                $namecd2 = $row["NAMECD2"];
                $sdate = common::DateConv1(str_replace("-","/",$row["SDATE"]),0);
                $edate = common::DateConv1(str_replace("-","/",$row["EDATE"]),0);
                if( $namecd1=="A002" ){
                    if( $namecd2==1 || $namecd2==2 || $namecd2==3 ){            //入学
                        $enterdate1 = $sdate."　　第１学年　入学";
                    }
                    if( $namecd2==4 ){                                          //転入学
                        $transferdate1 = $sdate."　　第".sprintf("%d",$row["GRADE"])."学年転入学";
                    }
                }
                if( $namecd1=="A004" ){
                    if( $namecd2==4 ){                                          //編入
                        $enterdate2 = $sdate."　　第".sprintf("%d",$row["GRADE"])."学年編入学";
                        $enterreason2 = $row["REASON"];
                    }
                    if( $namecd2==1 ){                                          //留学
                        if($ia > 2) continue;
                        $ia++;
                        $strx = $sdate;
                        if( $edate != null )    $strx = $strx."～".$edate;
                        if( $ia==1 ){
                            $transferdate3_1 = $strx;
                            $transferreason3_1 = $row["REASON"];
                            $transferplace3_1 = $row["PLACE"];
                        }
                        if( $ia==2 ){
                            $transferdate3_2 = $strx;
                            $transferreason3_2 = $row["REASON"];
                            $transferplace3_2 = $row["PLACE"];
                        }
                    }
                    if( $namecd2==2 ){                                          //休学
                        if($ia > 2) continue;
                        $ia++;
                        $strx = $sdate;
                        if( $edate != null )    $strx = $strx."～".$edate;
                        if( $ia==1 ){
                            $transferdate3_1 = $strx;
                            $transferreason3_1 = $row["REASON"];
                            $transferplace3_1 = $row["PLACE"];
                        }
                        if( $ia==2 ){
                            $transferdate3_2 = $strx;
                            $transferreason3_2 = $row["REASON"];
                            $transferplace3_2 = $row["PLACE"];
                        }
                    }
                }
                if( $namecd1=="A003" ){
                    if( $namecd2==3 ){                                          //転学
                        $transferdate2 = $sdate."　　第".sprintf("%d",$row["GRADE"])."学年　転学";
                        $transferreason2 = $row["REASON"];
                    }
                    if( $namecd2==2 ){                                          //退学
                        $transferdate2 = $sdate."　　第".sprintf("%d",$row["GRADE"])."学年　退学";
                        $transferreason2 = $row["REASON"];
                    }
                    if( $namecd2==1 ){                                          //卒業
                        $transferdate4 = $sdate;
                        $transferplace4 = $row["PLACE"];
                    }
                }

                $data = true;
            }

            // 最高年次 //
            $query = knja131bQuery::getinfo_max_grade($this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                $annual_max = sprintf("%d",$row["ANNUAL_MAX"]);
            }
            $annual_show = $nendo = $annual = $hr_class = $attendno = $principalname = $staffname = array();
            for($j=0;$j<$annual_max;$j++){
                $annual_show[$j]    = $j + 1;

                $nendo[$j]          = "　";
                $annual[$j]         = "　";
                $hr_class[$j]       = "　";
                $attendno[$j]       = "　";
                $principalname[$j]  = "　";
                $staffname[$j]      = "　";
            }
            // 学籍等履歴 //
            $query = knja131bQuery::getinfo_ps5_1($this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                $annual_int = (Int)sprintf("%d",$row["ANNUAL"]) - 1;

                $nendo[$annual_int]         = common::DateConv1($row["YEAR"]."/04/01",10);
                $annual[$annual_int]        = $row["ANNUAL"];
                $hr_class[$annual_int]      = $row["HR_CLASS"];
                $attendno[$annual_int]      = $row["ATTENDNO"];
                $principalname[$annual_int] = $row["PRINCIPALNAME"];
                $staffname[$annual_int]     = $row["STAFFNAME"];

                $data = true;
            }
            // 個人学籍データ //
            $query = knja131bQuery::getinfo_ps2_1($this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                //１・２・３行目
                $contents .= ",,,,,,";
                ($this->control["学校区分"]=="0") 
                    ? $contents .= "学　　年," 
                    : $contents .= "年　　次,";
                $contents .= implode($annual_show, ",") ."\n";
                $contents .= ",高　等　学　校　生　徒　指　導　要　録,,,,,";
                $contents .= "ﾎｰﾑﾙｰﾑ,".implode($hr_class, ",") ."\n";
                $contents .= ",,,,,,";
                $contents .= "整理番号,".implode($attendno, ",") ."\n";
                $contents .= "\n";

                //５行目
                $contents .= ",,,,,学籍の記録,," ."\n";
                $contents .= "生徒" ."\n";
                //６・７・８・９行目
                $contents .= "ふりがな,".$row["NAME_KANA"].",,,,,入学・,".$enterdate1."\n";
                $contents .= "氏名,";
                ($this->field["simei"]=="1") 
                    ? $contents .= $row["NAME"].",,,,,編入学・,".$enterdate2."\n" 
                    : $contents .= ",,,,,編入学・,".$enterdate2."\n";
                $contents .= "生年月日,".common::DateConv1(str_replace("-","/",$row["BIRTHDAY"]),0)
                            ."生,,,,,再入学等,".$enterreason2."\n";
                $contents .= "性別,".$row["SEX"]."\n";
                //１０・１１・１２行目
                ( strlen($s_zip_new) > 0 ) 
                    ? $s_zip_new = "〒".$s_zip_new 
                    : $s_zip_new = "　".$s_zip_new;
                $contents .= "現住所,".$s_zip_new.",,,,,転入学等,".$transferdate1."\n";
                $s_addr_new = $s_addr1_new.$s_addr2_new;
                if( strlen($s_addr_new) > 60 ){
                    $contents .= ",".$s_addr1_new."\n";
                    $contents .= ",　".$s_addr2_new.",,,,,転学・,".$transferdate2."\n";
                } else {
                    $contents .= ",".$s_addr_new."\n";
                    $contents .= ",,,,,,転学・,".$transferdate2."\n";
                }
                //１３・１４・１５行目
                ( strlen($s_zip_old1) > 0 ) 
                    ? $s_zip_old1 = "〒".$s_zip_old1 
                    : $s_zip_old1 = "　".$s_zip_old1;
                $contents .= "現住所,".$s_zip_old1.",,,,,退学等,".$transferreason2."\n";
                $s_addr_old1 = $s_addr1_old1.$s_addr2_old1;
                if( strlen($s_addr_old1) > 60 ){
                    $contents .= "(前),".$s_addr1_old1."\n";
                    $contents .= ",　".$s_addr2_old1.",,,,,留学・,".$transferdate3_1."\n";
                } else {
                    $contents .= "(前),".$s_addr_old1."\n";
                    $contents .= ",,,,,,留学・,".$transferdate3_1."\n";
                }
                //１６・１７・１８行目
                ( strlen($s_zip_old2) > 0 ) 
                    ? $s_zip_old2 = "〒".$s_zip_old2 
                    : $s_zip_old2 = "　".$s_zip_old2;
                $contents .= "現住所,".$s_zip_old2.",,,,,休学等,".$transferreason3_1."\n";
                $s_addr_old2 = $s_addr1_old2.$s_addr2_old2;
                if( strlen($s_addr_old2) > 60 ){
                    $contents .= "(前前),".$s_addr1_old2.",,,,,,".$transferplace3_1."\n";
                    $contents .= ",　".$s_addr2_old2.",,,,,,".$transferdate3_2."\n";
                } else {
                    $contents .= "(前前),".$s_addr_old2.",,,,,,".$transferplace3_1."\n";
                    $contents .= ",,,,,,,".$transferdate3_2."\n";
                }
                //１９・２０行目
                $contents .= "保護者,,,,,,,".$transferreason3_2."\n";
                $contents .= "ふりがな,".$row["GUARD_KANA"].",,,,,,".$transferplace3_2."\n";
                $contents .= "氏名,";
                ($this->field["simei"]=="1") 
                    ? $contents .= $row["GUARD_NAME"]."\n" 
                    : $contents .= "　"."\n";
                //２１・２２・２３行目
                $contents .= "現住所,";
                if( $row["ADDR"] == $row["GUARD_ADDR"] ){
                    $contents .= "生徒の欄に同じ,,,,,卒　　業,".$transferdate4."\n";
                    $contents .= ",,,,,,,,卒業台帳番号（".$transferplace4."）"."\n\n";
                } else {
                    ( strlen($row["GUARD_ZIPCD"]) > 0 ) 
                        ? $row["GUARD_ZIPCD"] = "〒".$row["GUARD_ZIPCD"] 
                        : $row["GUARD_ZIPCD"] = "　".$row["GUARD_ZIPCD"];
                    $contents .= $row["GUARD_ZIPCD"].",,,,,卒　　業,".$transferdate4."\n";
                    if( strlen($row["GUARD_ADDR"]) > 60 ){
                        $contents .= ",".$row["GUARD_ADDR1"].",,,,,,,卒業台帳番号（".$transferplace4."）"."\n";
                        $contents .= ",　".$row["GUARD_ADDR2"]."\n";
                    } else {
                        $contents .= ",".$row["GUARD_ADDR"].",,,,,,,卒業台帳番号（".$transferplace4."）"."\n\n";
                    }
                }
                //２４・２５行目
                $contents .= "入学前,".common::DateConv1(str_replace("-","/",$row["FINISH_DATE"]),2)
                            .",,,,,進学先・"."\n";
                $contents .= "の経歴,,".$row["J_NAME"]."中学校卒業,,,,就職先等"."\n";
                $contents .= "\n";

                //２７行目
                $contents .= "学校名,".$this->control["学校名1"];
                $contents .= ",,,,,年　　度,".implode($nendo, ",") ."\n";
                //２８・２９・３０行目
                $contents .= "所在地,〒".$this->control["学校郵便番号"];
                ($this->control["学校区分"]=="0") 
                    ? $contents .= ",,,,,学　　年," 
                    : $contents .= ",,,,,年　　次,";
                $contents .= implode($annual, ",") ."\n";
                $school_addr = $this->control["学校住所1"].$this->control["学校住所2"];
                ( strlen($school_addr) > 60 ) 
                    ? $contents .= ",".$this->control["学校住所1"] 
                    : $contents .= ",".$school_addr;
                $contents .= ",,,,,校　　長".",".implode($principalname, ",") ."\n";
                ( strlen($school_addr) > 60 ) 
                    ? $contents .= ",　".$this->control["学校住所2"] 
                    : $contents .= ",";
                $contents .= ",,,,,氏 名 印"."," ."\n";
                //３１・３２行目
                $contents .= "課程名,".$row["COURSENAME"];
                $contents .= ",,,,,ﾎｰﾑﾙｰﾑ担,".implode($staffname, ",") ."\n";
                $contents .= "学科名,".$row["MAJORNAME"];
                $contents .= ",,,,,任者氏名印,　" ."\n";
                $contents .= "\n";
                $contents .= "\n";

                $data = true;
            }

            $result->free();

        return $contents;
    }


    //２．修得単位の記録
    function getResult2($db,$i)
    {
        // 学習記録データ //
        $data = false;
        $subclasscd = "000000";

        $contents .= "各教科・科目等の修得単位数の記録"."\n";
        $contents .= "教科,科目,修得単位数の計"."\n";

        $query = knja131bQuery::getinfo_ps1_2($this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            if($row["SUBCLASSCD"]==$subclasscd) continue;
            $subclasscd = $row["SUBCLASSCD"];

            if($row["CLASSNAME"]!="sogo" && $row["CLASSNAME"]!="abroad" && $row["CLASSNAME"]!="total"){
                $contents .= $row["CLASSNAME"].",";
                $contents .= $row["SUBCLASSNAME"].",";
                $contents .= $row["CREDIT"]."\n";
            } else {
                $credit = ( strlen($row["CREDIT"])>0 ) ? $row["CREDIT"] : 0;
            }

            if($row["CLASSNAME"]=="total")  $total  = $credit;
            if($row["CLASSNAME"]=="sogo")   $sogo   = $credit;
            if($row["CLASSNAME"]=="abroad") $abroad = $credit;

            $data = true;
        }

        if($data){
            $total_kei = (int)$total + (int)$sogo + (int)$abroad;
            $contents .= "小　　　　　　　　　計,,".$total."\n";
            $contents .= "総合的な学習の時間,,".$sogo."\n";
            $contents .= "留　　　　　　　　　学,,".$abroad."\n";
            $contents .= "修得単位数の合計,,".$total_kei."\n";
            $contents .= "\n";
            $contents .= "\n";
        }

        $result->free();

        return $contents;
    }


    //３．学習の記録
    function getResult3($db,$i)
    {
        $data = false;

        $contents .= "様　式　２　（　指　導　に　関　す　る　記　録　）"."\n";

            // 最高年次 //
            $query = knja131bQuery::getinfo_max_grade($this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                $annual_max = sprintf("%d",$row["ANNUAL_MAX"]);
            }
            $annual_show = $annual_show2 = $hr_class = $attendno = array();
            for($j=0;$j<$annual_max;$j++){
                $annual_show[$j]    = $j + 1;

                ($this->control["学校区分"]=="0") 
                    ? $annual_show2[$j]     = "　　　　第".$annual_show[$j]."学年" 
                    : $annual_show2[$j]     = "　　　　第".$annual_show[$j]."年次";
                
                $hr_class[$j]       = "　";
                $attendno[$j]       = "　";
            }
            // 学籍等履歴 //
            $query = knja131bQuery::getinfo_ps5_1($this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                $annual_int = (Int)sprintf("%d",$row["ANNUAL"]) - 1;

                $hr_class[$annual_int]      = $row["HR_CLASS"];
                $attendno[$annual_int]      = $row["ATTENDNO"];

                $data = true;
            }
        // 個人学籍データ //
        $query = knja131bQuery::getinfo_ps2_3($this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            //学校名の文字数により、表示セル数を決める。
            if( strlen($this->control["学校名1"]) < 61 ){
                $contents .= ",,,,,,,,";
                ($this->control["学校区分"]=="0") 
                    ? $contents .= "区分＼学年," 
                    : $contents .= "区分＼年次,";
                $contents .= implode($annual_show, ",") ."\n";
                $contents .= "生徒氏名,".$row["NAME"].",,学校名,".$this->control["学校名1"].",,,,";
                $contents .= "ﾎｰﾑﾙｰﾑ,".implode($hr_class, ",") ."\n";
                $contents .= ",,,,,,,,";
                $contents .= "整理番号,".implode($attendno, ",") ."\n";
                $contents .= "\n";
            } elseif( strlen($this->control["学校名1"]) < 76 ) {
                $contents .= ",,,,,,,,,";
                ($this->control["学校区分"]=="0") 
                    ? $contents .= "区分＼学年," 
                    : $contents .= "区分＼年次,";
                $contents .= implode($annual_show, ",") ."\n";
                $contents .= "生徒氏名,".$row["NAME"].",,学校名,".$this->control["学校名1"].",,,,,";
                $contents .= "ﾎｰﾑﾙｰﾑ,".implode($hr_class, ",") ."\n";
                $contents .= ",,,,,,,,,";
                $contents .= "整理番号,".implode($attendno, ",") ."\n";
                $contents .= "\n";
            } else {
                $contents .= ",,,,,,,,,,";
                ($this->control["学校区分"]=="0") 
                    ? $contents .= "区分＼学年," 
                    : $contents .= "区分＼年次,";
                $contents .= implode($annual_show, ",") ."\n";
                $contents .= "生徒氏名,".$row["NAME"].",,学校名,".$this->control["学校名1"].",,,,,,";
                $contents .= "ﾎｰﾑﾙｰﾑ,".implode($hr_class, ",") ."\n";
                $contents .= ",,,,,,,,,,";
                $contents .= "整理番号,".implode($attendno, ",") ."\n";
                $contents .= "\n";
            }
            $data = true;
        }

        $contents .= ",,各　　　教　　　科　　　・　　　科　　　目　　　等　　　の　　　学　　　習　　　の　　　記　　　録"."\n";
        $contents .= ",,,,".implode($annual_show2, ",,").",,"."\n";
        $contents .= ",各　教　科 ・ 科　目　等,,,";
        for($k=0;$k<$annual_max;$k++) $contents .= "評　定,修　 得,";
        $contents .= "修得単位,,備　　　　考"."\n";
        $contents .= ",,,,";
        for($k=0;$k<$annual_max;$k++) $contents .= ",単位数,";
        $contents .= "数の計,"."\n";
        $contents .= "教科等,,科　目　等"."\n";

        $hyoutei = $tannisuu = array(); //評定・修得単位数
        $subclasscd = "000000"; //科目コードの変わり目の判断用
        $contents_keep = "";    //一時保管用

        $sogo = $total = $abroad = array(); //総合的な学習の時間・小計・留学の修得単位数
        $goukei = array();  //合計の修得単位数
        //（初期値）
        for($j=1;$j<($annual_max+2);$j++){
            $sogo[$j]       = "　";
            $total[$j]      = "　";
            $abroad[$j]     = "　";
            $goukei[$j]     = 0;
        }

        $query = knja131bQuery::getinfo_ps3_3($this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            //教科・科目行
            if($row["CLASSNAME"]!="sogo" && $row["CLASSNAME"]!="abroad" && $row["CLASSNAME"]!="total"){
                //科目コードが同じなら、一時保管用をクリア
                if($row["SUBCLASSCD"]==$subclasscd){
                    $contents_keep = "";
                } else {
                    //出力用に一時保管用をセット（ループ１回目は除く）
                    if($subclasscd!="000000") $contents .= $contents_keep;
                    //評定・修得単位数をクリア（初期値）
                    for($j=1;$j<($annual_max+1);$j++){
                        $hyoutei[$j]        = "　";
                        $tannisuu[$j]       = "　";
                    }
                }
                //評定・修得単位数をセット（各学年）
                $annual_int = sprintf("%d",$row["ANNUAL"]);
                $hyoutei[$annual_int]   = $row["GRADES"];
                $tannisuu[$annual_int]  = $row["GRADE_CREDIT"];
                //一時保管用に値をセット
                $contents_keep  = $row["CLASSNAME"].",";
                $contents_keep .= $row["SUBCLASSNAME"].",,,";
                for($j=1;$j<($annual_max+1);$j++){
                    $contents_keep .= $hyoutei[$j] .",";
                    $contents_keep .= $tannisuu[$j] .",";
                }
                $contents_keep .= $row["CREDIT"]."\n";
                //判断用に科目コードをセット
                $subclasscd = $row["SUBCLASSCD"];
            //総合的な学習の時間・小計・留学・合計行
            } else {
                //修得単位数をセット（各学年）
                $annual_int = sprintf("%d",$row["ANNUAL"]);
                if($annual_int>0){
                    if($row["CLASSNAME"]=="sogo")   $sogo[$annual_int]      = $row["CREDIT"];
                    if($row["CLASSNAME"]=="total")  $total[$annual_int]     = $row["CREDIT"];
                    if($row["CLASSNAME"]=="abroad") $abroad[$annual_int]    = $row["CREDIT"];
                //修得単位数の計をセット
                } else {
                    if($row["CLASSNAME"]=="sogo")   $sogo[$annual_max+1]    = $row["CREDIT"];
                    if($row["CLASSNAME"]=="total")  $total[$annual_max+1]   = $row["CREDIT"];
                    if($row["CLASSNAME"]=="abroad") $abroad[$annual_max+1]  = $row["CREDIT"];
                }
            }

            $data = true;
        }

        if($contents_keep != "") $contents .= $contents_keep;
        $contents .= "\n";
        $contents .= ",総合的な学習の時間,,,";
        for($j=1;$j<($annual_max+1);$j++) $contents .= "／,".$sogo[$j] .",";
        $contents .= $sogo[$annual_max+1] ."\n";
        $contents .= ",　　　小　　　　計,,,";
        for($j=1;$j<($annual_max+1);$j++) $contents .= "／,".$total[$j] .",";
        $contents .= $total[$annual_max+1].",／,／,／," ."\n";
        $contents .= ",　　　留　　　　学,,,";
        for($j=1;$j<($annual_max+1);$j++) $contents .= "／,".$abroad[$j] .",";
        $contents .= $abroad[$annual_max+1].",／,／,／," ."\n";
        $contents .= ",　　　合　　　　計,,,";
        for($j=1;$j<($annual_max+2);$j++){
            if( is_numeric($sogo[$j]) ) $goukei[$j] = (int)$goukei[$j] + (int)$sogo[$j];
            if( is_numeric($total[$j]) ) $goukei[$j] = (int)$goukei[$j] + (int)$total[$j];
            if( is_numeric($abroad[$j]) ) $goukei[$j] = (int)$goukei[$j] + (int)$abroad[$j];
            if( $j > $annual_max ){
                ( $goukei[$j] > 0 ) 
                    ? $contents .= $goukei[$j].",／,／,／,"  ."\n"
                    : $contents .= "　,／,／,／," ."\n";
            } else {
                ( $goukei[$j] > 0 ) 
                    ? $contents .= "／,".$goukei[$j] ."," 
                    : $contents .= "／,　,";
            }
        }
        $contents .= "\n";
        $contents .= "\n";

        $result->free();

        return $contents;
    }


    //４．活動の記録
    function getResult4($db,$i)
    {
        // 活動の記録データ(生徒別) //
        $total_act1 = $total_act2 = $total_act3 = $total_act4 = "";
        $total_val1 = $total_val2 = $total_val3 = $total_val4 = $total_val5 = $total_val6 = "";

        $query = knja131bQuery::getinfo_ps5_4($this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);
            $row = str_replace("\r",",",$row);
            $row = str_replace("\n",",",$row);

            $tok1 = strtok($row["TOTALSTUDYACT"],",");
            $tok_no1 = 0;
            while( $tok1 ){
                if($tok_no1>3) break;
                if($tok_no1==0) $total_act1 = $tok1;
                if($tok_no1==1) $total_act2 = $tok1;
                if($tok_no1==2) $total_act3 = $tok1;
                if($tok_no1==3) $total_act4 = $tok1;
                $tok1 = strtok(",");
                $tok_no1++;
            }
            $tok2 = strtok($row["TOTALSTUDYVAL"],",");
            $tok_no2 = 0;
            while( $tok2 ){
                if($tok_no2>5) break;
                if($tok_no2==0) $total_val1 = $tok2;
                if($tok_no2==1) $total_val2 = $tok2;
                if($tok_no2==2) $total_val3 = $tok2;
                if($tok_no2==3) $total_val4 = $tok2;
                if($tok_no2==4) $total_val5 = $tok2;
                if($tok_no2==5) $total_val6 = $tok2;
                $tok2 = strtok(",");
                $tok_no2++;
            }

            $data = true;
        }

            // 最高年次 //
            $query = knja131bQuery::getinfo_max_grade($this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                $annual_max = sprintf("%d",$row["ANNUAL_MAX"]);
            }
            $annual_show = $annual_show2 = $spact_remark = $total_remark = $attend_remark = array();
            $attend_1 = $suspend = $mourning = $abroad = $requirepresent = $attend_6 = $present = array();
            for($j=0;$j<$annual_max;$j++){
                $annual_show[$j]    = $j + 1;

                ($this->control["学校区分"]=="0") 
                    ? $annual_show2[$j]     = "第".$annual_show[$j]."学年" 
                    : $annual_show2[$j]     = "第".$annual_show[$j]."年次";
                
            for($k=0;$k<6;$k++){
                $spact_remark[$j][$k]   = "";
                $total_remark[$j][$k]   = "";
            }
                $attend_remark[$j]  = "";
                $attend_1[$j]       = "";
                $suspend[$j]        = "";
                $mourning[$j]       = "";
                $abroad[$j]         = "";
                $requirepresent[$j] = "";
                $attend_6[$j]       = "";
                $present[$j]        = "";
            }
            // 活動の記録データ(学年別) //
            $query = knja131bQuery::getinfo_ps4_4($this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);
                $row = str_replace("\r",",",$row);
                $row = str_replace("\n",",",$row);
                $row["ATTENDREC_REMARK"] = str_replace(",","",$row["ATTENDREC_REMARK"]);

                $annual_int = (Int)sprintf("%d",$row["ANNUAL"]) - 1;

                $tok1 = strtok($row["SPECIALACTREMARK"],",");
                $tok_no1 = 0;
                while( $tok1 ){
                    if($tok_no1>5) break;
                    $spact_remark[$annual_int][$tok_no1] = $tok1;
                    $tok1 = strtok(",");
                    $tok_no1++;
                }
                $tok1 = strtok($row["TOTALREMARK"],",");
                $tok_no1 = 0;
                while( $tok1 ){
                    if($tok_no1>5) break;
                    $total_remark[$annual_int][$tok_no1] = $tok1;
                    $tok1 = strtok(",");
                    $tok_no1++;
                }
                $attend_remark[$annual_int] = $row["ATTENDREC_REMARK"];

                $data = true;
            }
            // 出欠の記録データ //
            $query = knja131bQuery::getinfo_ps3_4($this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                $annual_int = (Int)sprintf("%d",$row["ANNUAL"]) - 1;

                $attend_1[$annual_int]          = $row["ATTEND_1"];
                $suspend[$annual_int]           = $row["SUSPEND"];
                $mourning[$annual_int]          = $row["MOURNING"];
                $abroad[$annual_int]            = $row["ABROAD"];
                $requirepresent[$annual_int]    = $row["REQUIREPRESENT"];
                $attend_6[$annual_int]          = $row["ATTEND_6"];
                $present[$annual_int]           = $row["PRESENT"];

                $data = true;
            }

        // 個人学籍データ //
        $query = knja131bQuery::getinfo_ps2_3($this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $contents .= "生徒氏名,".$row["NAME"]."\n";

            $contents .= ",,,総 合 的 な 学 習 の 時 間 の 記 録"."\n";
            $contents .= "学習活動,".$total_act1."\n";
            $contents .= ",".$total_act2."\n";
            $contents .= ",".$total_act3."\n";
            $contents .= ",".$total_act4."\n";
            $contents .= "評価,". $total_val1."\n";
            $contents .= ",".$total_val2."\n";
            $contents .= ",". $total_val3."\n";
            $contents .= ",". $total_val4."\n";
            $contents .= ",".$total_val5."\n";
            $contents .= ",". $total_val6."\n";

            $contents .= ",,,特　　別　　活　　動　　の　　記　　録"."\n";
            $contents .= implode($annual_show2, ",,")."\n";
            for($k=0;$k<6;$k++){
                for($j=0;$j<$annual_max;$j++){
                    $contents .= $spact_remark[$j][$k];
                    ( $j==((Int)$annual_max-1) ) ? $contents .= "\n" : $contents .= ",,";
                }
            }
            $contents .= ",,総　合　所　見　及　び　指　導　上　参　考　と　な　る　諸　事　項"."\n";
            for($j=0;$j<$annual_max;$j++){
                $contents .= $annual_show2[$j].",".$total_remark[$j][0]."\n";
                $contents .= ",".$total_remark[$j][1]."\n";
                $contents .= ",".$total_remark[$j][2]."\n";
                $contents .= ",".$total_remark[$j][3]."\n";
                $contents .= ",".$total_remark[$j][4]."\n";
                $contents .= ",".$total_remark[$j][5]."\n";
            }
            $contents .= ",,,出　　　欠　　　の　　　記　　　録"."\n";
            ($this->control["学校区分"]=="0") 
                ? $contents .= "学年" 
                : $contents .= "年次";
            $contents .= "＼区分,授業日数,出席停止の日数,忌引の日数,留学中の授業日数,出席しなければならない日数,欠席日数,出席日数,備考"."\n";
            for($j=0;$j<$annual_max;$j++){
                $contents .= $annual_show[$j].",";
                $contents .= $attend_1[$j].",";
                $contents .= $suspend[$j].",";
                $contents .= $mourning[$j].",";
                $contents .= $abroad[$j].",";
                $contents .= $requirepresent[$j].",";
                $contents .= $attend_6[$j].",";
                $contents .= $present[$j].",";
                $contents .= $attend_remark[$j]."\n";
            }
            $contents .= "（注）,「総合所見及び指導上参考となる諸事項」の欄には、以下のような事項などを記録する。"."\n";
            $contents .= ",(1)各教科・科目や総合的な学習の時間,,,,(5)生徒が就職している場合の事業所"."\n";
            $contents .= ",　　の学習に関する所見,,,,(6)生徒の特徴・特技、部活動、学校内外におけるボランティア活動、"."\n";
            $contents .= ",(2)行動に関する所見,,,,　　表彰を受けた行為や活動、標準検査に関する記録など指導上参考"."\n";
            $contents .= ",(3)進路指導に関する事項,,,,　　となる諸事項"."\n";
            $contents .= ",(4)取得資格,,,,(7)生徒の成長の状況にかかわる総合的な所見"."\n";
            $contents .= "\n";
            $contents .= "\n";

            $data = true;
        }

        $result->free();

        return $contents;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
