<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje070bModel extends Model {
    var $field      = array();
    var $select_data     = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $school_name;    //学校名

    /**
     * メンバ変数の初期化
     */
    function knje070bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("DATE"           =>  VARS::post("DATE"),            //処理年月日
                            "YEAR"            =>  VARS::post("YEAR"),            //年度
                            "GAKKI"           =>  VARS::post("GAKKI"),           //学期コード
                            "OUTPUT"          =>  VARS::post("OUTPUT"),          //調査書種類
                            "OS"              =>  VARS::post("OS"),              //所見入力時ＯＳ
                            "KANJI"           =>  VARS::post("KANJI"),           //漢字氏名印刷
                            "SONOTAJUUSYO"    =>  VARS::post("SONOTAJUUSYO"),    //その他住所を優先して印字する
                            "MIRISYU"         =>  VARS::post("MIRISYU"),         //未履修科目出力
                            "RISYU"           =>  VARS::post("RISYU"),           //履修のみ科目出力
                            "TANIPRINT_SOUGOU"  => VARS::post("TANIPRINT_SOUGOU"), //総合的な学習の時間の単位を０表示ラジオ
                            "TANIPRINT_RYUGAKU" => VARS::post("TANIPRINT_RYUGAKU"),//留学の単位を０表示ラジオ
                            "FORM6"           =>  VARS::post("FORM6"),           //フォーム選択
                            "useSyojikou3"    =>  VARS::post("useSyojikou3"),    //指導上参考となる諸事欄、３分割フォーム
                            "tyousasyoNotPrintAnotherAttendrec"    =>  VARS::post("tyousasyoNotPrintAnotherAttendrec"),    //出欠の前籍校を含まないチェックボックスを作成する
                            "KOTYO"           =>  VARS::post("KOTYO"),           //校長印刷
                            "GRADE_HR_CLASS"  =>  VARS::post("GRADE_HR_CLASS"),  //年組
                            "HYOTEI"          =>  VARS::post("HYOTEI"),          //評定の処理設定
                            "COMMENT"         =>  VARS::post("COMMENT"),         //学習成績概評
                            "SEKI"            =>  VARS::post("SEKI"),            //記載責任者
                            "DBNAME"          =>  VARS::post("DBNAME")           //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->cmd         = VARS::request("cmd");
        $db = Query::dbCheckOut();
        $query = knje070bQuery::getSchoolName();
        $this->school_name = $db->getOne($query);
        Query::dbCheckIn($db);
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期値の設定
        $this->Properties["useSyojikou3"]                      = ($this->Properties["useSyojikou3"] == '1')               ? $this->Properties["useSyojikou3"]                      : 0;
        $this->Properties["tyousasyoTaniPrint"]                = ($this->Properties["tyousasyoTaniPrint"] == '2')         ? $this->Properties["tyousasyoTaniPrint"]                : 1;
        $this->Properties["useCertifSchPrintCnt"]              = ($this->Properties["useCertifSchPrintCnt"] == '1')       ? $this->Properties["useCertifSchPrintCnt"]              : 0;
        $this->Properties["tyousasyoAttendrecRemarkFieldSize"] = ($this->Properties["tyousasyoAttendrecRemarkFieldSize"]) ? $this->Properties["tyousasyoAttendrecRemarkFieldSize"] : "";
        $this->Properties["train_ref_1_2_3_field_size"]        = ($this->Properties["train_ref_1_2_3_field_size"])        ? $this->Properties["train_ref_1_2_3_field_size"]        : "";
        $this->Properties["nenYoForm"]                         = ($this->Properties["nenYoForm"])                         ? $this->Properties["nenYoForm"]                         : "6";
        $this->Properties["tyousasyoSougouHyoukaNentani"]      = ($this->Properties["tyousasyoSougouHyoukaNentani"])      ? $this->Properties["tyousasyoSougouHyoukaNentani"]      : "";
        $this->Properties["tyousasyoKinsokuForm"]              = ($this->Properties["tyousasyoKinsokuForm"])              ? $this->Properties["tyousasyoKinsokuForm"]              : "";
    }

    function getDownloadModel()
    {
        //CSVデータ抽出処理
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";    //CSVデータ出力用
        $data = false;

        //学籍番号ループ
        for($i=0;$i<get_count($this->select_data["selectdata"]);$i++){
            if( $this->field["OUTPUT"] == "1" ) $contents .= $this->getResult1($db,$i);    //１．進学用
            if( $this->field["OUTPUT"] == "2" ) $contents .= $this->getResult2($db,$i);    //２．就職用
            if($contents!="") $data = true;
        }//学籍番号ループ

        //DB切断
        Query::dbCheckIn($db);

        if ($data){
            if( $this->field["OUTPUT"] == "1" ) $couse="調査書(進学用)";
            if( $this->field["OUTPUT"] == "2" ) $couse="調査書(就職用)";
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


    //改行マークが1つでもないときは、文字数をカウントして、改行して出力する。2004-08-26 naka
    function getKaigyo($tai_data,$len_data)
    {
        $pos = strpos( $tai_data, "\n" );        //改行マークのチェック
        $len_start = strlen( $tai_data );        //対象データのバイト数
        if( $pos===false && $len_start>$len_data ){
            $len_end = 0;
            $newtext_tst = "";
            while( $len_end<$len_start ){
                $newtext_tst .= substr( $tai_data, $len_end, $len_data ) ."\n";
                $len_end = $len_end+$len_data;
            }
            $tai_data = $newtext_tst;
        }
        return $tai_data;
    }
    //１．進学用
    function getResult1($db,$i)
    {
        $contents = "";
        $contents1 = $contents2 = $contents3 = $contents4 = $contents5 = "";
        $contents6 = $contents7 = $contents8 = $contents9 = $contents10 = $contents11 = "";
        $data = false;

        // 学校データ //
        $year2 = common::DateConv1($this->field["DATE"],12);    //過卒生対応年度取得
        $kisai_date = common::DateConv1($this->field["DATE"],0);    //記載日付
        if ($year2 == "") $year2 = CTRL_YEAR;    //過卒生対応年度取得 空白の場合 2005.11.04---とりあえず今年度をセットしておく
        if ($kisai_date == "") $kisai_date = "　　　年　月　日";    //記載日付 空白の場合 2005.11.04

        $query = knje070bQuery::getinfo_ps7($this,$year2,$this->field["SEKI"], $this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $clsf_ctn = $row["CLASSIFICATION"];
            $schoolname1 = $row["SCHOOLNAME1"];
            ( strlen($row["SCHOOLZIPCD"]) > 0 ) ? $schoolzipcd = "〒".$row["SCHOOLZIPCD"] : $schoolzipcd = "";
            $schooladdr1 = $row["SCHOOLADDR1"];
            $schooladdr2 = $row["SCHOOLADDR2"];
            $principal_name = $row["PRINCIPAL_NAME"];
            $staff2_jobname = $row["STAFF2_JOBNAME"];
            $staff2_name = $row["STAFF2_NAME"];
            ( $row["SCHOOLDIV"]=="0" ) ? $anname = "学年" : $anname = "年次";
            $schooldiv = $row["SCHOOLDIV"];
        }    //while

        // 個人データ //
        $query = knje070bQuery::getinfo_ps6($this, $this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $contents1 .= "１．生徒情報"."\n";
            $contents1 .= "ふりがな,".$row["NAME_KANA"].",,,,性別,".$row["SEX"]."\n";
            $contents1 .= "氏名,";
            if( $this->field["KANJI"]=="1" ) $contents1 .= $row["NAME"];
            $contents1 .= ",,,,現住所,";
            $contents1 .= $row["ADDR1"]."\n";
            $contents1 .= "生年月日,".common::DateConv1(str_replace("-","/",$row["BIRTHDAY"]),0)."生";
            $contents1 .= ",,,,,".$row["ADDR2"]."\n";
            $contents1 .= "学校名,".$clsf_ctn.",".$schoolname1.",,,";
            $contents1 .= "在学期間,".common::DateConv1(str_replace("-","/",$row["ENT_DATE"]),1).",,";
            if ($row["ENT_DIV"]=="1" ||
                $row["ENT_DIV"]=="2" ||
                $row["ENT_DIV"]=="3") $row["ENTER_NAME"] = "入学";
            $contents1 .= "第".sprintf("%d",$row["ENTER_GRADE"]).$anname."　".$row["ENTER_NAME"]."\n";
            $contents1 .= "課程名,".$row["COURSENAME"].",学科名,".$row["MAJORNAME"].",,";
            $contents1 .= ",".common::DateConv1(str_replace("-","/",$row["GRADU_DATE"]),1).",,";
            $contents1 .= $row["GRADU_NAME"]."\n";
        }    //while

        // 最高年次 //
        $query = knje070bQuery::getinfo_max_grade($this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $annual_max = sprintf("%d",$row["ANNUAL_MAX"]);
        }
        $annual_show = $annual_show2 = $annual_show3 = $spe_act_rec = $train_ref = $attend_remark = array();
        $attend_1 = $susp_mour = $abroad = $requirepresent = $attend_6 = $present = array();
        $hyoutei_name = $hyoutei_val = $surashu = array();
        for($j=0;$j<$annual_max;$j++){
            $annual_show[$j]    = $j + 1;
            $annual_show2[$j]   = $annual_show[$j].$anname;
            $annual_show3[$j]   = "第".$annual_show[$j].$anname;

            $attend_1[$j]       = "";
            $susp_mour[$j]      = "";
            $abroad[$j]         = "";
            $requirepresent[$j] = "";
            $attend_6[$j]       = "";
            $present[$j]        = "";
            $attend_remark[$j]  = "";

            for($k=0;$k<8;$k++){
                $spe_act_rec[$j][$k] = "";
            }
            for($k=0;$k<5;$k++){
                $train_ref[$j][$k]   = "";
            }

            $hyoutei_name[$j] = "評定";
            $hyoutei_val[$j]  = "";
            $surashu[$j]      = "／";
        }

        $total_ac = $total_val = $remark = array();
        for($k=0;$k<2;$k++){
            $total_ac[$k]     = "";
        }
        for($k=0;$k<3;$k++){
            $total_val[$k]     = "";
        }
        for($k=0;$k<5;$k++){
            $remark[$k]     = "";
        }
        $doc_remark = $tr_remark = $disease = "";

        // 所見データ //
        $query = knje070bQuery::getinfo_ps4_1($this, $this->select_data["selectdata"][$i],$schooldiv);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
/***********************************2004-08-26 naka (miyagiより修正依頼)**********************************/
            /*改行マークが1つでもないときは、文字数をカウントして、改行して出力する。*/
            //６．特別活動の記録
            $tai_data = $row["SPECIALACTREC"];        //対象データ
            $len_data = 33;                            //対象データの1行のバイト数
            $row["SPECIALACTREC"] = $this->getKaigyo($tai_data,$len_data);        //対象データ（リターン値）
            //７．指導上参考となる諸事項
            $tai_data = $row["TRAIN_REF"];
            $tai_data2 = $row["TRAIN_REF2"];
            $tai_data3 = $row["TRAIN_REF3"];
            $len_data = 123;
            $row["TRAIN_REF"] = $this->getKaigyo($tai_data,$len_data);
            $row["TRAIN_REF2"] = $this->getKaigyo($tai_data2,$len_data);
            $row["TRAIN_REF3"] = $this->getKaigyo($tai_data3,$len_data);
            //８．総合的な学習の時間の内容・評価
            $tai_data = $row["TOTALSTUDYACT"];
            $len_data = 123;
            $row["TOTALSTUDYACT"] = $this->getKaigyo($tai_data,$len_data);
            $tai_data = $row["TOTALSTUDYVAL"];
            $len_data = 123;
            $row["TOTALSTUDYVAL"] = $this->getKaigyo($tai_data,$len_data);
            //９．備考
            $tai_data = $row["REMARK"];
            $len_data = 123;
            $row["REMARK"] = $this->getKaigyo($tai_data,$len_data);
/***********************************2004-08-26 naka (miyagiより修正依頼)**********************************/
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
               $row = str_replace(",",".",$row);
               $row = str_replace("\r",",",$row);
                  $row = str_replace("\n",",",$row);
               $row["ATTENDREC_REMARK"] = str_replace(",","",$row["ATTENDREC_REMARK"]);
               $row["DOC_REMARK"] = str_replace(",","",$row["DOC_REMARK"]);
               $row["TR_REMARK"] = str_replace(",","",$row["TR_REMARK"]);
               $row["DISEASE"] = str_replace(",","",$row["DISEASE"]);

            $annual_int = sprintf("%d",$row["ANNUAL"]) - 1;
            //出欠備考
            $attend_remark[$annual_int] = $row["ATTENDREC_REMARK"];
            //担当学校医所見
            $doc_remark = $row["DOC_REMARK"];
            //担任所見
            $tr_remark = $row["TR_REMARK"];
            //疾患及び異常
            $disease = $row["DISEASE"];
            //特別活動の記録
                  $tok = strtok($row["SPECIALACTREC"],",");
            $tok_no = 0;
            while( $tok ){
                if($tok_no>7) break;
                $spe_act_rec[$annual_int][$tok_no] = $tok;
                      $tok = strtok(",");
                $tok_no++;
            }
            //指導上参考となる諸事項
            $tok  = strtok($row["TRAIN_REF"],",");
            $tok2 = strtok($row["TRAIN_REF2"],",");
            $tok3 = strtok($row["TRAIN_REF3"],",");
            $tok_no = 0;
            while ($tok || $tok2 || $tok3) {
                if ($tok_no > 7) {
                    break;
                }
                $tok  = strtok($row["TRAIN_REF"],",");  for ($j = 0; $j < $tok_no; $j++) { $tok = strtok(","); }
                $tok2 = strtok($row["TRAIN_REF2"],","); for ($j = 0; $j < $tok_no; $j++) { $tok2 = strtok(","); }
                $tok3 = strtok($row["TRAIN_REF3"],","); for ($j = 0; $j < $tok_no; $j++) { $tok3 = strtok(","); }
                $train_ref[$annual_int][$tok_no] = $tok.",".$tok2.",".$tok3;
                $tok_no++;
            }
            //総合的な学習の時間の内容・評価
            $tok = strtok($row["TOTALSTUDYACT"],",");
            $tok_no = 0;
            while( $tok ){
                if($tok_no>1) break;
                $total_ac[$tok_no] = $tok;
                      $tok = strtok(",");
                $tok_no++;
               }
            $tok = strtok($row["TOTALSTUDYVAL"],",");
            $tok_no = 0;
            while( $tok ){
                if($tok_no>2) break;
                $total_val[$tok_no] = $tok;
                      $tok = strtok(",");
                $tok_no++;
            }
            //備考
            $tok = strtok($row["REMARK"],",");
            $tok_no = 0;
            while( $tok ){
                if($tok_no>4) break;
                $remark[$tok_no] = $tok;
                $tok = strtok(",");
                $tok_no++;
            }
        }

        $contents7 .= "６．特別活動の記録"."\n";
        $contents7 .= implode($annual_show2, ",,,")."\n";
        for($k=0;$k<8;$k++){
            for($j=0;$j<$annual_max;$j++){
                   $contents7 .= $spe_act_rec[$j][$k];
                ( $j==($annual_max-1) ) ? $contents7 .= "\n" : $contents7 .= ",,,";
            }
        }

        $contents8 .= "７．指導上参考となる諸事項"."\n";
        $train_ref_gyou = "";
        if ($this->Properties["train_ref_1_2_3_gyo_size"]) {
            $train_ref_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
        } else {
            $train_ref_gyou = "5";
        }
        for($j=0;$j<$annual_max;$j++){
               $contents8 .= $annual_show2[$j].",".$train_ref[$j][0]."\n";
               for($k=1;$k<$train_ref_gyou;$k++) $contents8 .= ",".$train_ref[$j][$k]."\n";
        }

        $contents9 .= "８．総合的な学習の時間の内容・評価"."\n";
        $contents9 .= "活動内容,".$total_ac[0]."\n";
        $contents9 .= ",".$total_ac[1]."\n";
        $contents9 .= "評価,".$total_val[0]."\n";
        $contents9 .= ",".$total_val[1]."\n";
        $contents9 .= ",".$total_val[2]."\n";

        $contents10 .= "９．備考"."\n";
        for($k=0;$k<5;$k++) $contents10 .= ",".$remark[$k]."\n";

        // 出欠の記録データ //
        $query = knje070bQuery::getinfo_ps2($this->select_data["selectdata"][$i],$schooldiv);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $annual_int = sprintf("%d",$row["ANNUAL"]) - 1;

            $attend_1[$annual_int]             = $row["ATTEND_1"];
            $susp_mour[$annual_int]         = $row["SUSP_MOUR"];
            $abroad[$annual_int]             = $row["ABROAD"];
            $requirepresent[$annual_int]     = $row["REQUIREPRESENT"];
            $attend_6[$annual_int]             = $row["ATTEND_6"];
            $present[$annual_int]             = $row["PRESENT"];
        }
        $contents5 .= "５．出欠の記録" ."\n";
        $contents5 .= $anname."＼区分,授業日数,出席停止・忌引の日数,留学中の授業日数,出席しなければならない日数,欠席日数,出席日数,備考"."\n";
        for($j=0;$j<$annual_max;$j++){
            $contents5 .= $annual_show2[$j].",";
            $contents5 .= $attend_1[$j].",";
            $contents5 .= $susp_mour[$j].",";
            $contents5 .= $abroad[$j].",";
            $contents5 .= $requirepresent[$j].",";
            $contents5 .= $attend_6[$j].",";
            $contents5 .= $present[$j].",";
            $contents5 .= $attend_remark[$j]."\n";
        }

        // 学習記録データ //
        $s_subclasscd = "00"; //科目コード
        $s_classcd = "00";    //教科コード
        $avg_line = 0;        //各教科の評定平均値の列番目
        $ad_credit = 0;       //加算単位
        $sogo_credit = 0;
        $classname = $avg_grades = array();
        $contents2_keep = $class_name = $subclass_name = "";

        $query = knje070bQuery::getinfo_ps1_1($this, $this->select_data["selectdata"][$i],$this->field["HYOTEI"],$this,$schooldiv);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            //総計
            if( $row["CLASSNAME"]=="total" ){
                ( strlen($row["AVG_GRADES"]) > 0 )         //全体の評定平均値
                    ? $average = $row["AVG_GRADES"]
                    : $average = 0.0;
                $assess_level = $row["ASSESS_LEVEL"];    //学習成績概評
                $ass_lev_maru = $row["SUBCLASSNAME"];    //特Ａ
                continue;
            }
            //総計
            if( $row["CLASSNAME"]=="totalCredit" ){
                if( is_numeric($row["CREDIT"]) ) $ad_credit += $row["CREDIT"];    //修得単位数
                continue;
            }
            //留学
            if( $row["CLASSNAME"]=="abroad" ){
                $abroad_credit = $row["CREDIT"];    //修得単位数
                if( is_numeric($row["CREDIT"]) ) $ad_credit += $row["CREDIT"];
                continue;
            }
            //総合
            if( $row["CLASSNAME"]=="sogo" ){
                $sogo_credit += $row["CREDIT"];        //修得単位数
                if( is_numeric($row["CREDIT"]) ) $ad_credit += $row["CREDIT"];
                continue;
            }
            $annual_int = sprintf("%d",$row["ANNUAL"]);
            if( $annual_int == "0" )    continue;
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $row["CLASSCD"] = $row["CLASSCD"].$row["SCHOOL_KIND"];
                $row["SUBCLASSCD"] = $row["CLASSCD"].$row["SUBCLASSCD"];
            }
            //科目コードのブレイク
            if( $row["SUBCLASSCD"]!=$s_subclasscd ) {
                if( $s_subclasscd!="00" ) {
                    $contents2_keep .= $class_name.",".$subclass_name.",,".implode($hyoutei_val, ",").",".$kei_credit."\n";
                    for($j=0;$j<$annual_max;$j++) $hyoutei_val[$j] = "";
                }
                $s_subclasscd = $row["SUBCLASSCD"];
                $subclass_name = $row["SUBCLASSNAME"];        //科目名
                $kei_credit = $row["CREDIT"];                 //修得単位数
            }
            //教科コードのブレイク
            if( $row["CLASSCD"]!=$s_classcd ){
                $s_classcd = $row["CLASSCD"];
                $class_name = $row["CLASSNAME"];              //教科名
                //各教科の評定平均値の出力
                $classname[$avg_line] = $row["CLASSNAME"];    //教科名
                $avg_grades[$avg_line] = $row["AVG_GRADES"];  //平均値
                $avg_line++;
            }

            //科目評定(学年)の出力
            $annual_int = $annual_int - 1;
            $hyoutei_val[$annual_int] = $row["GRADES"] ? intval($row["GRADES"]) : '';       //評定

            $data = true;
        }    //while

        if( $data ){
            $contents2_keep .= $class_name.",".$subclass_name.",,".implode($hyoutei_val, ",").",".$kei_credit."\n";
        }
        $contents2 .= "２．各教科・科目の学習の記録"."\n";
        $contents2 .= "教科・科目,,,";
        $contents2 .= "評定"."\n";
        $contents2 .= "教科,科目,,";
        $contents2 .= implode($annual_show3, ",").",";
        $contents2 .= "修得単位数の計"."\n";
        $contents2 .= $contents2_keep;
        $contents2 .= ",総合的な学習の時間,,";
        $contents2 .= implode($surashu, ",").",";
        $contents2 .= $sogo_credit."\n";
        $contents2 .= ",留学,,";
        $contents2 .= implode($surashu, ",").",";
        $contents2 .= $abroad_credit."\n";
        $contents2 .= ",計,,";
        $contents2 .= implode($surashu, ",").",";
        $contents2 .= $ad_credit;
        $contents2 .= "\n";

        $contents3 .= "３．各教科の評定平均値"."\n";
        $contents3 .= "教科,".implode($classname, ",").",全体の評定平均値"."\n";
        $contents3 .= "平均値,".implode($avg_grades, ",").",".$average."\n";

        // 成績概評人数データ //
        $query = knje070bQuery::getinfo_ps5($this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $contents4 .= "４．学習成績概評,,成績段階別人数"."\n";
            $contents4 .= $ass_lev_maru.$assess_level."　段階,,";
            $contents4 .= "Ａ　".$row["MEMBER5"]."人,";
            $contents4 .= "Ｂ　".$row["MEMBER4"]."人,";
            $contents4 .= "Ｃ　".$row["MEMBER3"]."人,";
            $contents4 .= "Ｄ　".$row["MEMBER2"]."人,";
            $contents4 .= "Ｅ　".$row["MEMBER1"]."人,";
            //学年人数の表示・非表示    2004/04/27
            if( $this->field["COMMENT"]=="on" ){
                $contents4 .= "合計　".$row["MEMBER0"]."人（".$row["MEMBER6"]."人）";
            } else {
                $contents4 .= "合計　".$row["MEMBER0"]."人";
            }
            $contents4 .= "\n";

        }

        $result->free();

        $contents11 .= "この調査書の記載事項に誤りがないことを証明する"."\n";
        $contents11 .= ",".$kisai_date."\n";
        $contents11 .= "学校名,".$schoolname1."\n";
        $contents11 .= "所在地,".$schoolzipcd."　".$schooladdr1.$schooladdr2."\n";
        $contents11 .= "校長名,".$principal_name.",,印,記載責任者職氏名,,".$staff2_jobname."　".$staff2_name.",,,印"."\n";

        $contents .= "証第　　号,,調査書"."\n";
        $contents .= $contents1."\n".$contents2."\n".$contents3."\n".$contents4."\n".$contents5."\n".$contents6."\n";
        $contents .= $contents7."\n".$contents8."\n".$contents9."\n".$contents10."\n".$contents11."\n\n";

        return $contents;

    }


    //２．就職用
    function getResult2($db,$i)
    {
           $contents = "";
           $contents1 = $contents2 = $contents5 = $contents6 = $contents7 = $contents10 = $contents11 = "";
        $data = false;

        // 学校データ //
        $year2 = common::DateConv1($this->field["DATE"],12);    //過卒生対応年度取得
        $kisai_date = common::DateConv1($this->field["DATE"],0);    //記載日付
        if ($year2 == "") $year2 = CTRL_YEAR;    //過卒生対応年度取得 空白の場合 2005.11.04---とりあえず今年度をセットしておく
        if ($kisai_date == "") $kisai_date = "　　　年　月　日";    //記載日付 空白の場合 2005.11.04

        $query = knje070bQuery::getinfo_ps7($this,$year2,$this->field["SEKI"], $this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $schoolname1 = $row["SCHOOLNAME1"];
            ( strlen($row["SCHOOLZIPCD"]) > 0 ) ? $schoolzipcd = "〒".$row["SCHOOLZIPCD"] : $schoolzipcd = "";
            $schooladdr1 = $row["SCHOOLADDR1"];
            $schooladdr2 = $row["SCHOOLADDR2"];
            $principal_name = $row["PRINCIPAL_NAME"];
            $staff2_jobname = $row["STAFF2_JOBNAME"];
            $staff2_name = $row["STAFF2_NAME"];
            ( $row["SCHOOLDIV"]=="0" ) ? $anname = "学年" : $anname = "年次";
            $schooldiv = $row["SCHOOLDIV"];
        }

        // 個人データ //
        $query = knje070bQuery::getinfo_ps6($this, $this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $contents1 .= "ふりがな,".$row["NAME_KANA"].",,,,性別,".$row["SEX"]."\n";
            $contents1 .= "氏名,";
            if( $this->field["KANJI"]=="1" ) $contents1 .= $row["NAME"];
            $contents1 .= ",,,,現住所,";
            $contents1 .= $row["ADDR1"]."\n";
            $contents1 .= "生年月日,".common::DateConv1(str_replace("-","/",$row["BIRTHDAY"]),0)."生";
            $contents1 .= ",,,,,".$row["ADDR2"]."\n";
            $contents1 .= "学校名,".$schoolname1.",,,,";
            $contents1 .= "在学期間,".common::DateConv1(str_replace("-","/",$row["ENT_DATE"]),1).",,";
            if ($row["ENT_DIV"]=="1" ||
                $row["ENT_DIV"]=="2" ||
                $row["ENT_DIV"]=="3") $row["ENTER_NAME"] = "入学";
            $contents1 .= "第".sprintf("%d",$row["ENTER_GRADE"]).$anname."　".$row["ENTER_NAME"]."\n";
            $contents1 .= "課程名,".$row["COURSENAME"].",学科名,".$row["MAJORNAME"].",,";
            $contents1 .= ",".common::DateConv1(str_replace("-","/",$row["GRADU_DATE"]),1).",,";
            $contents1 .= $row["GRADU_NAME"]."\n";
        }

        // 最高年次 //
        $query = knje070bQuery::getinfo_max_grade($this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $annual_max = sprintf("%d",$row["ANNUAL_MAX"]);
        }
        $annual_show = $annual_show2 = $annual_show3 = array();
        $attend_6 = array();
        $hyoutei_name = $hyoutei_val = $abroad_val = array();
        for($j=0;$j<$annual_max;$j++){
            $annual_show[$j]   = $j + 1;
            $annual_show2[$j]  = $annual_show[$j].$anname;
            $annual_show3[$j]  = "第".$annual_show[$j].$anname;

            $attend_6[$j]      = "";

            $hyoutei_name[$j]  = "評定";
            $hyoutei_val[$j]   = "";
            $abroad_val[$j]    = "";
        }

        $jobhunt_health = $jobhunt_absence = $jobhunt_rec = $jobhunt_recommend = array();
        for($k=0;$k<3;$k++)  $jobhunt_health[$k]    = "";
        for($k=0;$k<4;$k++)  $jobhunt_absence[$k]   = "";
        for($k=0;$k<8;$k++)  $jobhunt_rec[$k]       = "";
        for($k=0;$k<10;$k++) $jobhunt_recommend[$k] = "";

        // 所見データ //
        $query = knje070bQuery::getinfo_ps4_2($this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
               $row = str_replace(",",".",$row);
               $row = str_replace("\r",",",$row);
                  $row = str_replace("\n",",",$row);

            //特別活動の記録
                $tok = strtok($row["JOBHUNT_REC"],",");
            $tok_no = 0;
            while( $tok ){
                if($tok_no>7) break;
                $jobhunt_rec[$tok_no] = $tok;
                $tok = strtok(",");
                $tok_no++;
            }
            //欠席の主な理由
                $tok = strtok($row["JOBHUNT_ABSENCE"],",");
            $tok_no = 0;
            while( $tok ){
                if($tok_no>3) break;
                $jobhunt_absence[$tok_no] = $tok;
                $tok = strtok(",");
                $tok_no++;
            }
            //身体状況備考
                $tok = strtok($row["JOBHUNT_HEALTHREMARK"],",");
            $tok_no = 0;
            while( $tok ){
                if($tok_no>2) break;
                $jobhunt_health[$tok_no] = $tok;
                $tok = strtok(",");
                $tok_no++;
            }
            //本人の長所・推薦事由等
                $tok = strtok($row["JOBHUNT_RECOMMEND"],",");
            $tok_no = 0;
            while( $tok ){
                if($tok_no>9) break;
                $jobhunt_recommend[$tok_no] = $tok;
                $tok = strtok(",");
                $tok_no++;
            }
        }

        // 出欠の記録データ //
           $query = knje070bQuery::getinfo_ps2($this->select_data["selectdata"][$i],$schooldiv);
           $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

        //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
        $row = str_replace(",",".",$row);

        $annual_int = sprintf("%d",$row["ANNUAL"]) - 1;

        $attend_6[$annual_int]             = $row["ATTEND_6"];

        }

        $contents7 .= "特別活動の記録".",,,,,"."出席状況" ."\n";
        $contents7 .= $jobhunt_rec[0].",,,,,".",".implode($annual_show2, ",")."\n";
        $contents7 .= $jobhunt_rec[1].",,,,,"."欠席日数,".implode($attend_6, ",")."\n";
        $contents7 .= $jobhunt_rec[2]."\n";
        $contents7 .= $jobhunt_rec[3].",,,,,".",".$jobhunt_absence[0]."\n";
        $contents7 .= $jobhunt_rec[4].",,,,,"."欠 席 の,".$jobhunt_absence[1]."\n";
        $contents7 .= $jobhunt_rec[5].",,,,,"."主な理由,".$jobhunt_absence[2]."\n";
        $contents7 .= $jobhunt_rec[6].",,,,,".",".$jobhunt_absence[3]."\n";
        $contents7 .= $jobhunt_rec[7]."\n";

        $contents10 .= "本人の長所・推薦事由等"."\n";
        for($k=0;$k<10;$k++) $contents10 .= $jobhunt_recommend[$k]."\n";

        // 健康診断データ //
        $kensabi = $height = $weight = $r_barevision = $r_vision = $l_barevision = $l_vision = $r_ear = $l_ear = "";
           $query = knje070bQuery::getinfo_ps3($this->select_data["selectdata"][$i]);
              $result = $db->query($query);
           while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
               $row = str_replace(",",".",$row);

            $kensabi      = common::DateConv1(str_replace("-","/",$row["DATE"]),1);
            $height       = $row["HEIGHT"];
            $weight       = $row["WEIGHT"];
            $r_barevision = $row["R_BAREVISION"];
            $r_vision     = $row["R_VISION"];
            $l_barevision = $row["L_BAREVISION"];
            $l_vision     = $row["L_VISION"];
            $r_ear        = $row["R_EAR"];
            $l_ear        = $row["L_EAR"];

           }

            $contents6 .= "身体状況".",,,,,,検査日,".$kensabi."\n";
            $contents6 .= "身長,".$height." cm,"."視力,右 ".$r_barevision." （ ".$r_vision." ）".",聴力,右 ".$r_ear.",備考,".$jobhunt_health[0]."\n";
            $contents6 .= "体重,".$weight." kg,".",左 ".$l_barevision." （ ".$l_vision." ）".",,左 ".$l_ear.",,".$jobhunt_health[1]."\n";
            $contents6 .= ",,,,,,,".$jobhunt_health[2]."\n";
            $contents6 .= "(視力欄にＡ～Ｄが記入されている場合、Ａ：1.0以上、Ｂ：1.0未満0.7以上、Ｃ：0.7未満0.3以上、Ｄ：0.3未満を表す)"."\n";

        // 学習記録データ //
        $s_subclasscd = "00";    //科目コード
        $s_classcd = "00";        //教科コード
        $contents2_keep = $class_name = $subclass_name = "";
        $sogoCredit = array(0 => "", 1 => "", 2 => "");
        $abroad_val = array(0 => "", 1 => "", 2 => "");

           $query = knje070bQuery::getinfo_ps1_2($this->select_data["selectdata"][$i],$this->field["HYOTEI"],$this);
              $result = $db->query($query);
           while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
               $row = str_replace(",",".",$row);

                $annual_int = sprintf("%d",$row["ANNUAL"]);
                if( $annual_int == "0" || $row["CLASSNAME"]=="total" )    continue;
                //総合的な学習の時間
                if( $row["CLASSNAME"]=="sogo" ){
                    //科目評定(学年)の出力
                    $annual_int = $annual_int - 1;
                    $sogoCredit[$annual_int] = $row["CREDIT"];        //単位数
                    continue;
                }
                //留学
                if( $row["CLASSNAME"]=="abroad" ){
                    //科目評定(学年)の出力
                    $annual_int = $annual_int - 1;
                    $abroad_val[$annual_int] = $row["CREDIT"];        //単位数
                    continue;
                }
                //教育課程対応
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $row["CLASSCD"] = $row["CLASSCD"].$row["SCHOOL_KIND"];
                    $row["SUBCLASSCD"] = $row["CLASSCD"].$row["SUBCLASSCD"];
                }
                //科目コードのブレイク
                if( $row["SUBCLASSCD"]!=$s_subclasscd ){
                    if( $s_subclasscd!="00" ){
                        $contents2_keep .= $class_name.",".$subclass_name.",,".implode($hyoutei_val, ",")."\n";
                        for($j=0;$j<$annual_max;$j++) $hyoutei_val[$j] = "";
                    }
                    $s_subclasscd = $row["SUBCLASSCD"];
                    $subclass_name = $row["SUBCLASSNAME"];            //科目名
                }
                //教科コードのブレイク
                if( $row["CLASSCD"]!=$s_classcd ){
                    $s_classcd = $row["CLASSCD"];
                    $class_name = $row["CLASSNAME"];                //教科名
                }
                //科目評定(学年)の出力
                $annual_int = $annual_int - 1;
                $hyoutei_val[$annual_int] = $row["GRADES"];            //評定

            $data = true;

           }

        if( $data ){
            $contents2_keep .= $class_name.",".$subclass_name.",,".implode($hyoutei_val, ",")."\n";
        }
        $contents2 .= "学習の記録"."\n";
        $contents2 .= "教科・科目,,,";
        $contents2 .= "評定"."\n";
        $contents2 .= "教科,科目,,";
        $contents2 .= implode($annual_show3, ",")."\n";
        $contents2 .= $contents2_keep;
        $contents2 .= ",総合的な学習の時間による修得単位数,,";
        $contents2 .= implode($sogoCredit, ",")."\n";
        $contents2 .= ",留学による修得単位数,,";
        $contents2 .= implode($abroad_val, ",")."\n";

        $result->free();

        $contents11 .= "記載者,,".$staff2_jobname."　".$staff2_name.",,,,印"."\n\n";
        $contents11 .= "上記の記載事項に誤りがないことを証明します。"."\n";
        $contents11 .= ",".$kisai_date."\n\n";
        $contents11 .= ",(所在地),".$schoolzipcd."　".$schooladdr1.$schooladdr2."\n\n";
        $contents11 .= ",(学校名),".$schoolname1."\n";
        $contents11 .= ",(校長名),".$principal_name.",,,,印"."\n";

        $contents .= "証第　　号,,調査書"."\n";
        $contents .= $contents1."\n".$contents2."\n".$contents7."\n".$contents6."\n";
        $contents .= $contents10."\n".$contents11."\n\n";

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
