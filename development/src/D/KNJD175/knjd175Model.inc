<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd175Model extends Model {
    var $field      = array();
    var $select_data      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $output;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd175Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //異動対象日付 /* NO003 */
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト名/* NO001 */
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "CERTIF_DATE"      =>  VARS::post("CERTIF_DATE"),      //修了証記載日付
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //保護者の住所を出力する 2005.08.17
                             "OUTPUT2"          =>  VARS::post("OUTPUT2"),          //認定単位・委員会・部活動の表記なし 2005.09.14
                             "OUTPUT3"          =>  VARS::post("OUTPUT3"),          //遅刻回数・早退回数の表記なし 2005.09.28
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->cmd = VARS::request("cmd");
        $this->output = VARS::request("OUTPUT");
        $this->getPropertiesAll();
    }

    function getDownloadModel()
    {
        //ヘッダの作成//
        $db = Query::dbCheckOut();
        $query = knjd175Query::getSemester();
        $result = $db->query($query);

        $header2 = array();
        $header2_1 = array();
        $header2[] = "教科";
        $header2[] .= "科目";
        $header2[] .= "履修単位数";
        $header2_1[] = "　";
        $header2_1[] .= "　";
        $header2_1[] .= "　";

        $header3 = array();
        $header3[]  = "　";
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if($row["VALUE"] != "3"){
                $header2[] .= "中間";
                $header2[] .= "平均";
                if($this->control["学期数"]=="2") $header2[] .= "評価";
                for($k=0;$k<2;$k++) $header2_1[] .= $row["LABEL"];
                if($this->control["学期数"]=="2") $header2_1[] .= $row["LABEL"];
            }
            $header2[] .= "期末";
            $header2[] .= "平均";
            $header2[] .= "評価";
            $header2[] .= "実授業時数";
            $header2[] .= "欠課時数";
            for($k=0;$k<5;$k++) $header2_1[] .= $row["LABEL"];

            $header3[] .= $row["LABEL"];
        }
        $result->free();
        Query::dbCheckIn($db);

        $header2[] .= "評定";
        $header2[] .= "修得単位数";
        $header2[] .= "実授業時数";
        $header2[] .= "欠課時数";
        for($k=0;$k<2;$k++) $header2_1[] .= "　";
        for($k=0;$k<2;$k++) $header2_1[] .= "合計";

        $header3[] .= "合計";

        $header4 = array();
        $header4[]  = "科目名";
        $header4[] .= "内容・資格等";
        $header4[] .= "単位数";

        //CSVデータ抽出処理
        //DB接続
        $db = Query::dbCheckOut();

        $wareki_year = common::DateConv1($this->control["学籍処理日"],10);  //年度
        $query = knjd175Query::getPrincipal($this->control["年度"]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $principal = $row["STAFFNAME"]; //校長名
        }
        $contents = ""; //CSVデータ出力用
        $data = false;
        //学籍番号ループ
        for($i=0;$i<get_count($this->select_data["selectdata"]);$i++){
            // 生徒基本データ //
            $contents1  = "";
            $query = knjd175Query::getinfo_ps1($this->control["年度"],
                                                    $this->field["GAKKI"],
                                                    $this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);
                $contents1 .= ","."郵便番号".","."〒".$row["GUARD_ZIPCD"].",,,,,,,,"."年度".",".$wareki_year."\n";
                $contents1 .= ","."住所１".",".$row["GUARD_ADDR1"].",,,,,,,,"."通知票"."\n";
                $contents1 .= ","."住所２".",".$row["GUARD_ADDR2"].",,,,,,,,"."学校名".",".$this->control["学校名1"]."\n";
                $contents1 .= ","."氏名".",".$row["GUARD_NAME"]."　様".",,,,,,,,"."学校長".",".$principal."\n";
                $contents1 .= ",,,,,,,,,,"."担任".",".$row["STAFFNAME"].","."印"."\n";
                $contents1 .= ",,,,,,,,,,"."課程・学科".",".$row["COURSENAME"].",".$row["MAJORNAME"]."\n";
                $contents1 .= ",,,,,,,,,,"."年組番".",".$row["HR_NAME"].sprintf("%d",$row["ATTENDNO"])."番"."\n";
                $contents1 .= ",,,,,,,,,,"."氏名".",".$row["NAME"]."\n";

            }
            // 生徒別履修科目データ //
            $contents2  = "";
            $contents2 .= "\n";
            $contents2 .= "成績"."\n";
            $contents2 .= implode($header2_1, ",") ."\n";
            $contents2 .= implode($header2, ",") ."\n";
            $query = knjd175Query::getinfo_ps2($this->control["年度"],
                                                    $this->field["GAKKI"],
                                                    $this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            $classname = $subclassname = $subclasscd = array();
            $seiseki = 0;
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if( $seiseki==20 )  break;
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);
                $classname[]    = $row["CLASSNAME"];
                $subclassname[] = $row["SUBCLASSNAME"];
                $subclasscd[]   = $row["SUBCLASSCD"];
                $data = true;
                $seiseki++;
            }
            for($l=0;$l<$seiseki;$l++){
                $class      = $classname[$l].",";       //教科
                $subclass   = $subclassname[$l].",";    //科目
                // 生徒別科目別素点データ //
                $query = knjd175Query::getinfo_ps3($this->control["年度"],
                                                    $this->field["GAKKI"],
                                                    $this->select_data["selectdata"][$i],
                                                    $subclasscd[$l]);
                $result = $db->query($query);
                $tst1_1 = $tst2_1 = $tst1_2 = $tst2_2 = $tst3_2 = "　".",";
                $avg1_1 = $avg2_1 = $avg1_2 = $avg2_2 = $avg3_2 = "　".",";
                $grading1_1 = $grading2_1 = "　".",";
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace(",",".",$row);
                    $row["AVG"] = floor($row["AVG"]);
                    if($row["SEMESTER"]=="1" && $row["TESTKINDCD"]=="01"){
                        $tst1_1 = $row["SCORE"].",";
                        $avg1_1 = $row["AVG"].",";
                    }
                    if($row["SEMESTER"]=="2" && $row["TESTKINDCD"]=="01"){
                        $tst2_1 = $row["SCORE"].",";
                        $avg2_1 = $row["AVG"].",";
                    }
                    if($row["SEMESTER"]=="1" && $row["TESTKINDCD"]=="02"){
                        $tst1_2 = $row["SCORE"].",";
                        $avg1_2 = $row["AVG"].",";
                    }
                    if($row["SEMESTER"]=="2" && $row["TESTKINDCD"]=="02"){
                        $tst2_2 = $row["SCORE"].",";
                        $avg2_2 = $row["AVG"].",";
                    }
                    if($row["SEMESTER"]=="3" && $row["TESTKINDCD"]=="02"){
                        $tst3_2 = $row["SCORE"].",";
                        $avg3_2 = $row["AVG"].",";
                    }
                    if($this->control["学期数"]=="2"){
                        if($row["SEMESTER"]=="1" && $row["TESTKINDCD"]=="01") $grading1_1 = $row["VALUE"].",";
                        if($row["SEMESTER"]=="2" && $row["TESTKINDCD"]=="01") $grading2_1 = $row["VALUE"].",";
                    }
                }
                // 生徒別科目別成績期末学年データ //
                $query = knjd175Query::getinfo_ps4($this->control["年度"],
                                                    $this->field["GAKKI"],
                                                    $this->select_data["selectdata"][$i],
                                                    $subclasscd[$l]);
                $result = $db->query($query);
                $grading1_2 = $grading2_2 = $grading3_2 = "　".",";
                $total_grading = $total_credit = "　".",";
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace(",",".",$row);
                    if($row["SEMESTER"]=="9"){
                        //2004/07/07 現在学期がSCHOOL_MSTのSEMESDIVと等しい場合評定を出力する
                        if($this->field["GAKKI"]==$this->control["学期数"])
                            $total_grading = $row["VALUATION"].",";     //評定
                        $total_credit  = $row["GET_CREDIT"].",";    //修得単位数
                    } else {
                        if($row["SEMESTER"]=="1") $grading1_2 = $row["VALUATION"].",";  //1学期評価
                        if($row["SEMESTER"]=="2") $grading2_2 = $row["VALUATION"].",";  //2学期評価
                        if($row["SEMESTER"]=="3") $grading3_2 = $row["VALUATION"].",";  //3学期評価
                    }
                }
                // 生徒別科目別授業時数・欠課時数データ //
                $query = knjd175Query::getinfo_ps5($this->control["年度"],
                                                    $this->field["GAKKI"],
                                                    $this->select_data["selectdata"][$i],
                                                    $subclasscd[$l]);
                $result = $db->query($query);
                $kekka1 = $kekka2 = $kekka3 = "　".",";
                $real_lesson1 = $real_lesson2 = $real_lesson3 = "　".",";
                $credit = $total_kekka = $total_real_lesson = "　".",";
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace(",",".",$row);
                    if($row["SEMESTER"]=="9"){
                        $credit             = $row["CREDITS"].",";  //履修単位数
                        $total_kekka        = $row["ABSENT"].",";   //欠課時数
                        $total_real_lesson  = $row["JISU"].",";     //実授業時数
                    } else {
                        if($row["SEMESTER"]=="1"){
                            $kekka1         = $row["ABSENT"].",";   //1学期欠課時数
                            $real_lesson1   = $row["JISU"].",";     //1学期実授業時数
                        }
                        if($row["SEMESTER"]=="2"){
                            $kekka2         = $row["ABSENT"].",";   //2学期欠課時数
                            $real_lesson2   = $row["JISU"].",";     //2学期実授業時数
                        }
                        if($row["SEMESTER"]=="3"){
                            $kekka3         = $row["ABSENT"].",";   //3学期欠課時数
                            $real_lesson3   = $row["JISU"].",";     //3学期実授業時数
                        }
                    }
                }
                $contents2 .= $class;
                $contents2 .= $subclass;
                $contents2 .= $credit;
                $contents2 .= $tst1_1;
                $contents2 .= $avg1_1;
                if($this->control["学期数"]=="2") $contents2 .= $grading1_1;
                $contents2 .= $tst1_2;
                $contents2 .= $avg1_2;
                $contents2 .= $grading1_2;
                $contents2 .= $real_lesson1;
                $contents2 .= $kekka1;
                $contents2 .= $tst2_1;
                $contents2 .= $avg2_1;
                if($this->control["学期数"]=="2") $contents2 .= $grading2_1;
                $contents2 .= $tst2_2;
                $contents2 .= $avg2_2;
                $contents2 .= $grading2_2;
                $contents2 .= $real_lesson2;
                $contents2 .= $kekka2;
                if($this->control["学期数"]!="2"){
                    $contents2 .= $tst3_2;
                    $contents2 .= $avg3_2;
                    $contents2 .= $grading3_2;
                    $contents2 .= $real_lesson3;
                    $contents2 .= $kekka3;
                }
                $contents2 .= $total_grading;
                $contents2 .= $total_credit;
                $contents2 .= $total_real_lesson;
                $contents2 .= $total_kekka;
                $contents2 .= "\n";
            }
            // 生徒別出欠データ //
            $classdays1 = $classdays2 = $classdays3 = $classdays4 = "　".",";           //授業日数
            $suspend1 = $suspend2 = $suspend3 = $suspend4 = "　".",";                   //特別欠席・忌引
            $mourning1 = $mourning2 = $mourning3 = $mourning4 = "　".",";               //特別欠席・出席停止
            $a_classdays1 = $a_classdays2 = $a_classdays3 = $a_classdays4 = "　".",";   //留学中の授業日数
            $present1_1 = $present1_2 = $present1_3 = $present1_4 = "　".",";           //出席すべき日数
            $absent1 = $absent2 = $absent3 = $absent4 = "　".",";                       //欠席日数
            $present2_1 = $present2_2 = $present2_3 = $present2_4 = "　".",";           //出席日数
            $late1 = $late2 = $late3 = $late4 = "　".",";                               //遅刻回数
            $early1 = $early2 = $early3 = $early4 = "　".",";                           //早退回数

            $query = knjd175Query::getinfo_ps6($this->control["年度"],
                                                    $this->field["GAKKI"],
                                                    $this->select_data["selectdata"][$i],
                                                    $this->field["GRADE_HR_CLASS"],
                                                    str_replace("/","-",$this->control["学期終了日付"]["9"]));
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                if($row["SEMESTER"]=="1"){  //1学期
                    $classdays1     = $row["CLASSDAYS"].",";
                    $suspend1       = $row["SUSPEND"].",";
                    $mourning1      = $row["MOURNING"].",";
                    $a_classdays1   = $row["ABROAD_CLASSDAYS"].",";
                    $present1_1     = $row["PRESENT1"].",";
                    $absent1        = $row["ABSENT"].",";
                    $present2_1     = $row["PRESENT2"].",";
                    $late1          = $row["LATE"].",";
                    $early1         = $row["EARLY"].",";
                }
                if($row["SEMESTER"]=="2"){  //2学期
                    $classdays2     = $row["CLASSDAYS"].",";
                    $suspend2       = $row["SUSPEND"].",";
                    $mourning2      = $row["MOURNING"].",";
                    $a_classdays2   = $row["ABROAD_CLASSDAYS"].",";
                    $present1_2     = $row["PRESENT1"].",";
                    $absent2        = $row["ABSENT"].",";
                    $present2_2     = $row["PRESENT2"].",";
                    $late2          = $row["LATE"].",";
                    $early2         = $row["EARLY"].",";
                }
                if($row["SEMESTER"]=="3"){  //3学期
                    $classdays3     = $row["CLASSDAYS"].",";
                    $suspend3       = $row["SUSPEND"].",";
                    $mourning3      = $row["MOURNING"].",";
                    $a_classdays3   = $row["ABROAD_CLASSDAYS"].",";
                    $present1_3     = $row["PRESENT1"].",";
                    $absent3        = $row["ABSENT"].",";
                    $present2_3     = $row["PRESENT2"].",";
                    $late3          = $row["LATE"].",";
                    $early3         = $row["EARLY"].",";
                }
                if($row["SEMESTER"]=="9"){  //合計
                    $classdays4     = $row["CLASSDAYS"].",";
                    $suspend4       = $row["SUSPEND"].",";
                    $mourning4      = $row["MOURNING"].",";
                    $a_classdays4   = $row["ABROAD_CLASSDAYS"].",";
                    $present1_4     = $row["PRESENT1"].",";
                    $absent4        = $row["ABSENT"].",";
                    $present2_4     = $row["PRESENT2"].",";
                    $late4          = $row["LATE"].",";
                    $early4         = $row["EARLY"].",";
                }

            }
            // 学校外における学修の単位認定データ //
            $ka1 = $ka2 = $ka3 = $ka4 = $ka5 = $ka6 = $ka7 = $ka8 = $ka9 = ",,,,,,";

            $query = knjd175Query::getinfo_ps7($this->control["年度"],$this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            $sub = 0;
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if( $sub==9 )   break;
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);
                if( $sub==0 ) $ka1 = ",,".$row["SUBCLASSNAME"].",".$row["CONTENTS"].",".$row["CREDITS"].",,";
                if( $sub==1 ) $ka2 = ",,".$row["SUBCLASSNAME"].",".$row["CONTENTS"].",".$row["CREDITS"].",,";
                if( $sub==2 ) $ka3 = ",,".$row["SUBCLASSNAME"].",".$row["CONTENTS"].",".$row["CREDITS"].",,";
                if( $sub==3 ) $ka4 = ",,".$row["SUBCLASSNAME"].",".$row["CONTENTS"].",".$row["CREDITS"].",,";
                if( $sub==4 ) $ka5 = ",,".$row["SUBCLASSNAME"].",".$row["CONTENTS"].",".$row["CREDITS"].",,";
                if( $sub==5 ) $ka6 = ",,".$row["SUBCLASSNAME"].",".$row["CONTENTS"].",".$row["CREDITS"].",,";
                if( $sub==6 ) $ka7 = ",,".$row["SUBCLASSNAME"].",".$row["CONTENTS"].",".$row["CREDITS"].",,";
                if( $sub==7 ) $ka8 = ",,".$row["SUBCLASSNAME"].",".$row["CONTENTS"].",".$row["CREDITS"].",,";
                if( $sub==8 ) $ka9 = ",,".$row["SUBCLASSNAME"].",".$row["CONTENTS"].",".$row["CREDITS"].",,";
                $sub++;
            }
            // 所見及び通信欄データ //
            $query = knjd175Query::getinfo_ps8($this->control["年度"],
                                                $this->field["GAKKI"],
                                                $this->select_data["selectdata"][$i]);
            $result = $db->query($query);
            $t_s_time = "総合的な学習の時間".",";
            $c_cation = "通信欄".",";
            $t_s_time1 = $t_s_time2 = $t_s_time3 = $t_s_time4 = $t_s_time5 = ",\n";
            $c_cation1 = $c_cation2 = $c_cation3 = $c_cation4 = $c_cation5 = ",\n";
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);
                $row = str_replace("\r\n",",",$row);
                $t_s_time .= $row["TOTALSTUDYTIME"].",";
                $c_cation .= $row["COMMUNICATION"].",";
            }
            $t_count = 5 - substr_count($t_s_time,",");
            $c_count = 5 - substr_count($c_cation,",");
            for($j=0;$j<$t_count;$j++) $t_s_time .= "　".",";
            for($j=0;$j<$c_count;$j++) $c_cation .= "　".",";
            $tok1 = strtok($t_s_time,",");
            $tok_no1 = 0;
            while( $tok1 ){
                if($tok_no1==0) $t_s_time1 = $tok1."\n";
                if($tok_no1==1) $t_s_time2 = $tok1."\n";
                if($tok_no1==2) $t_s_time3 = $tok1."\n";
                if($tok_no1==3) $t_s_time4 = $tok1."\n";
                if($tok_no1==4) $t_s_time5 = $tok1."\n";
                $tok1 = strtok(",");
                $tok_no1++;
            }
            $tok2 = strtok($c_cation,",");
            $tok_no2 = 0;
            while( $tok2 ){
                if($tok_no2==0) $c_cation1 = $tok2."\n";
                if($tok_no2==1) $c_cation2 = $tok2."\n";
                if($tok_no2==2) $c_cation3 = $tok2."\n";
                if($tok_no2==3) $c_cation4 = $tok2."\n";
                if($tok_no2==4) $c_cation5 = $tok2."\n";
                $tok2 = strtok(",");
                $tok_no2++;
            }

            $contents3  = "";
            $contents3 .= "\n\n";
            $contents3 .= "出欠状況".",,,,,,";
            if($this->control["学期数"]!="2") $contents3 .= ",";
            $contents3 .= "学校外における学修の単位認定".",,,,,".$t_s_time1;
            $contents3 .= implode($header3, ",") .",,,";
            $contents3 .= implode($header4, ",") .",,,".$t_s_time2;

            $ka1 .= ",".$t_s_time3;
            $ka2 .= ",".$t_s_time4;
            $ka3 .= ",".$t_s_time5;
            $ka4 .= ","."\n";
            $ka5 .= ",".$c_cation1;
            $ka6 .= ",".$c_cation2;
            $ka7 .= ",".$c_cation3;
            $ka8 .= ",".$c_cation4;
            $ka9 .= ",".$c_cation5;
            $contents3 .= "授業日数,".$classdays1.$classdays2;
            $contents3 .= ($this->control["学期数"]=="2") ? $classdays4.$ka1 : $classdays3.$classdays4.$ka1;
            $contents3 .= "特別欠席・忌引,".$suspend1.$suspend2;
            $contents3 .= ($this->control["学期数"]=="2") ? $suspend4.$ka2 : $suspend3.$suspend4.$ka2;
            $contents3 .= "特別欠席・出席停止,".$mourning1.$mourning2;
            $contents3 .= ($this->control["学期数"]=="2") ? $mourning4.$ka3 : $mourning3.$mourning4.$ka3;
            $contents3 .= "留学中の授業日数,".$a_classdays1.$a_classdays2;
            $contents3 .= ($this->control["学期数"]=="2") ? $a_classdays4.$ka4 : $a_classdays3.$a_classdays4.$ka4;
            $contents3 .= "出席すべき日数,".$present1_1.$present1_2;
            $contents3 .= ($this->control["学期数"]=="2") ? $present1_4.$ka5 : $present1_3.$present1_4.$ka5;
            $contents3 .= "欠席日数,".$absent1.$absent2;
            $contents3 .= ($this->control["学期数"]=="2") ? $absent4.$ka6 : $absent3.$absent4.$ka6;
            $contents3 .= "出席日数,".$present2_1.$present2_2;
            $contents3 .= ($this->control["学期数"]=="2") ? $present2_4.$ka7 : $present2_3.$present2_4.$ka7;
            $contents3 .= "遅刻回数,".$late1.$late2;
            $contents3 .= ($this->control["学期数"]=="2") ? $late4.$ka8 : $late3.$late4.$ka8;
            $contents3 .= "早退回数,".$early1.$early2;
            $contents3 .= ($this->control["学期数"]=="2") ? $early4.$ka9 : $early3.$early4.$ka9;

            if($seiseki > 0){
                $contents .= $contents1;
                $contents .= $contents2;
                $contents .= $contents3;
            }
            $contents .= "\n\n\n";
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        if ($data){
            $couse="通知票";
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

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
