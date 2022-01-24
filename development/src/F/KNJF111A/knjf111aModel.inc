<?php

require_once('for_php7.php');

class knjf111aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    function knjf111aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHKIND"  =>  VARS::post("SCHKIND"),  //校種
                             "SDATE"    =>  VARS::post("SDATE"),    //利用期間（開始日付）
                             "EDATE"    =>  VARS::post("EDATE"),    //利用期間（終了日付）
                             "YEAR"     =>  VARS::post("YEAR"),     //年度
                             "DBNAME"   =>  VARS::post("DBNAME")    //DB変更に備えて
                            );

        $this->field["selectdata"] = explode(",", VARS::post("selectdata"));

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //テーブルの有無チェック
        $query = knjf111aQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && (($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $this->Properties["use_prg_schoolkind"] == "1")) {
            //生徒項目名取得
            $schName = $db->getOne(knjf111aQuery::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);
 
        $this->kubunList = array();
        $this->kubunList[]= array('label' => '1:内科',          'value' => '1', 'label2' => '内科');
        $this->kubunList[]= array('label' => '2:外科',          'value' => '2', 'label2' => '外科');
        $this->kubunList[]= array('label' => '3:健康相談',      'value' => '5', 'label2' => '健康相談');
        $this->kubunList[]= array('label' => '4:その他',        'value' => '3', 'label2' => 'その他');
        $this->kubunList[]= array('label' => '5:'.$this->sch_label.'以外',      'value' => '4', 'label2' => $this->sch_label.'以外');

        $this->cmd = VARS::request("cmd");
    }

    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",CTRL_DATE),10);
        $title = "症状別保健室利用人数集計表";
        $wrk_date = common::DateConv1(str_replace("-","/",CTRL_DATE),3);
        $wrk_sdate = common::DateConv1(str_replace("-","/",$this->field["SDATE"]),3);
        $wrk_edate = common::DateConv1(str_replace("-","/",$this->field["EDATE"]),3);

        //学校名称2表示
        $schoolname2 = "";
        if (($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $this->Properties["use_prg_schoolkind"] == "1") {
            $schoolname2 = $db->getOne(knjf111aQuery::getSchoolName2($this))."　　";
        }

        $contents  = ",". $wrk_nendo ."　　". $schoolname2 . $title .",,,,,作成日：". $wrk_date ."\n";
        $contents .= "利用期間 : ". $wrk_sdate ." ～ ". $wrk_edate ."\n\n";

        //名称マスタのZ010を取得する
        $query = knjf111aQuery::getNameMst();
        $namespare2 = $db->getOne($query);

        //学年の配列を作る
        $query = knjf111aQuery::getGdat($this);
        $result = $db->query($query);
        $gdat_array = array();
        $grdName = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($namespare2 == '1') {
                $gdat_array[$row["SCHOOL_KIND"]][] = $row["GRADE"];
            } else {
                $gdat_array["ZZZZZZ"][] = $row["GRADE"]; //ZZZZZZ じゃなくても同じ文字列になるなら何でもいい
            }
            $grdName[$row["GRADE"]] = $row["GRADE_NAME1"];
        }

        $header1 = array();
        $header1[] = "利用区分";
        $header1[] = "症状名";

        $header2 = array();
        $header2[] = "";
        $header2[] = "";
        foreach ($gdat_array as $school_kind => $grade_array) {
            foreach($grade_array as $grade) {
                $header1[] = ($grdName[$grade]) ? $grdName[$grade] : "{$grade}年";
                $header1[] = "";
                $header1[] = "";

                $header2[] = "男";
                $header2[] = "女";
                $header2[] = "計";
            }
            $header1[] = "合計";
            $header1[] = "";
            $header1[] = "";

            $header2[] = "男";
            $header2[] = "女";
            $header2[] = "計";
        }
        $header1[] = "備考";
        $header2[] = "";

        $contents .= implode($header1, ",") ."\n";
        $contents .= implode($header2, ",") ."\n";

        /******************/
        /*     内  科     */
        /******************/
        if(in_array("1", $this->field["selectdata"])){
            //SQL文発行（来室理由の取得）
            $query = knjf111aQuery::getSelectName("F200");
            $result = $db->query($query);
            $opt_name1 = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
            }
            //出力データの作成//
            //SQL文発行
            $query = knjf111aQuery::selectCsvQuery($this, "1", "F200", $gdat_array);
            $result = $db->query($query);
            $data = false;
            //小計（内科）
            $naika_kei = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
                $row["KUBUN"] = '内科';
                $contents .= implode($row, ",") ."\n";
                //小計（内科）を計算
                foreach ($gdat_array as $school_kind => $grade_array) {
                    $kind = strtoupper($school_kind);
                    foreach($grade_array as $grade) {
                        $naika_kei["NUM_{$grade}M"] += $row["NUM_{$grade}M"];
                        $naika_kei["NUM_{$grade}F"] += $row["NUM_{$grade}F"];
                        $naika_kei["NUM_{$grade}T"] += $row["NUM_{$grade}T"];
                    }
                    $naika_kei["NUM_{$kind}M"] += $row["NUM_{$kind}M"];
                    $naika_kei["NUM_{$kind}F"] += $row["NUM_{$kind}F"];
                    $naika_kei["NUM_{$kind}T"] += $row["NUM_{$kind}T"];
                }

                $data = true;
            }
            //小計（内科）の行を出力
            $contents .= "内科,内科合計,";
            $contents .= implode($naika_kei, ",") ."\n";
        }

        /******************/
        /*     外  科     */
        /******************/
        if(in_array("2", $this->field["selectdata"])){
            //SQL文発行（来室理由の取得）
            $query = knjf111aQuery::getSelectName("F201");
            $result = $db->query($query);
            $opt_name1 = array();
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
            }
            //出力データの作成//
            //SQL文発行
            $query = knjf111aQuery::selectCsvQuery($this, "2", "F201", $gdat_array);
            $result = $db->query($query);
            $data = false;
            //小計（外科）
            $geka_kei = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
                $row["KUBUN"] = '外科';
                $contents .= implode($row, ",") ."\n";
                //小計（外科）を計算
                foreach ($gdat_array as $school_kind => $grade_array) {
                    $kind = strtoupper($school_kind);
                    foreach($grade_array as $grade) {
                        $geka_kei["NUM_{$grade}M"] += $row["NUM_{$grade}M"];
                        $geka_kei["NUM_{$grade}F"] += $row["NUM_{$grade}F"];
                        $geka_kei["NUM_{$grade}T"] += $row["NUM_{$grade}T"];
                    }
                    $geka_kei["NUM_{$kind}M"] += $row["NUM_{$kind}M"];
                    $geka_kei["NUM_{$kind}F"] += $row["NUM_{$kind}F"];
                    $geka_kei["NUM_{$kind}T"] += $row["NUM_{$kind}T"];
                }

                $data = true;
            }
            //小計（外科）の行を出力
            $contents .= "外科,外科合計,";
            $contents .= implode($geka_kei, ",") ."\n";
        }

        /**************/
        /*  健康相談  */
        /**************/
        if(in_array("5", $this->field["selectdata"])){
            //SQL文発行（来室理由の取得）
            $query = knjf111aQuery::getSelectName("F219");
            $result = $db->query($query);
            $opt_name1 = array();
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
            }
            //出力データの作成//
            //SQL文発行
            $query = knjf111aQuery::selectCsvQuery($this, "5", "F219", $gdat_array);
            $result = $db->query($query);
            $data = false;
            //小計（健康相談）
            $katsudo_kei = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
                $row["KUBUN"] = '健康相談';
                $contents .= implode($row, ",") ."\n";
                //小計（健康相談）を計算
                foreach ($gdat_array as $school_kind => $grade_array) {
                    $kind = strtoupper($school_kind);
                    foreach($grade_array as $grade) {
                        $katsudo_kei["NUM_{$grade}M"] += $row["NUM_{$grade}M"];
                        $katsudo_kei["NUM_{$grade}F"] += $row["NUM_{$grade}F"];
                        $katsudo_kei["NUM_{$grade}T"] += $row["NUM_{$grade}T"];
                    }
                    $katsudo_kei["NUM_{$kind}M"] += $row["NUM_{$kind}M"];
                    $katsudo_kei["NUM_{$kind}F"] += $row["NUM_{$kind}F"];
                    $katsudo_kei["NUM_{$kind}T"] += $row["NUM_{$kind}T"];
                }

                $data = true;
            }
            //小計（健康相談）の行を出力
            $contents .= "健康相談,健康相談合計,";
            $contents .= implode($katsudo_kei, ",") ."\n";
        }

        /******************/
        /*    そ の 他    */
        /******************/
        if(in_array("3", $this->field["selectdata"])){
            //SQL文発行（来室理由の取得）
            $query = knjf111aQuery::getSelectName("F203");
            $result = $db->query($query);
            $opt_name1 = array();
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
            }
            //出力データの作成//
            //SQL文発行
            $query = knjf111aQuery::selectCsvQuery($this, "3", "F203", $gdat_array);
            $result = $db->query($query);
            $data = false;
            //小計（その他）
            $sonota_kei = array();
            for($i=0; $i<12; $i++) $sonota_kei[$i] = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
                $row["KUBUN"] = 'その他';
                $contents .= implode($row, ",") ."\n";
                //小計（その他）を計算
                foreach ($gdat_array as $school_kind => $grade_array) {
                    $kind = strtoupper($school_kind);
                    foreach($grade_array as $grade) {
                        $sonota_kei["NUM_{$grade}M"] += $row["NUM_{$grade}M"];
                        $sonota_kei["NUM_{$grade}F"] += $row["NUM_{$grade}F"];
                        $sonota_kei["NUM_{$grade}T"] += $row["NUM_{$grade}T"];
                    }
                    $sonota_kei["NUM_{$kind}M"] += $row["NUM_{$kind}M"];
                    $sonota_kei["NUM_{$kind}F"] += $row["NUM_{$kind}F"];
                    $sonota_kei["NUM_{$kind}T"] += $row["NUM_{$kind}T"];
                }

                $data = true;
            }
            //小計（その他）の行を出力
            $contents .= "その他,その他合計,";
            $contents .= implode($sonota_kei, ",") ."\n";
        }

        /******************/
        /*    生徒以外    */
        /******************/
        if(in_array("4", $this->field["selectdata"])){
            //SQL文発行（来室理由の取得）
            $query = knjf111aQuery::getSelectName("F202");
            $result = $db->query($query);
            $opt_name1 = array();
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
            }
            //出力データの作成//
            //SQL文発行
            $query = knjf111aQuery::selectCsvQuery($this, "4", "F202", $gdat_array);
            $result = $db->query($query);
            $data = false;
            //小計（生徒以外）
            $igai_kei = array();
            for($i=0; $i<12; $i++) $igai_kei[$i] = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
                $row["KUBUN"] = $this->sch_label.'以外';
                $contents .= implode($row, ",") ."\n";
                //小計（生徒以外）を計算
                foreach ($gdat_array as $school_kind => $grade_array) {
                    $kind = strtoupper($school_kind);
                    foreach($grade_array as $grade) {
                        $igai_kei["NUM_{$grade}M"] += $row["NUM_{$grade}M"];
                        $igai_kei["NUM_{$grade}F"] += $row["NUM_{$grade}F"];
                        $igai_kei["NUM_{$grade}T"] += $row["NUM_{$grade}T"];
                    }
                    $igai_kei["NUM_{$kind}M"] += $row["NUM_{$kind}M"];
                    $igai_kei["NUM_{$kind}F"] += $row["NUM_{$kind}F"];
                    $igai_kei["NUM_{$kind}T"] += $row["NUM_{$kind}T"];
                }

                $data = true;
            }
            //小計（生徒以外）の行を出力
            $contents .= $this->sch_label."以外,".$this->sch_label."以外合計,";
            $contents .= implode($igai_kei, ",") ."\n";
        }

        //総合計の行を出力
        $sougou_kei = array();
        foreach ($gdat_array as $school_kind => $grade_array) {
            $kind = strtoupper($school_kind);
            foreach($grade_array as $grade) {
                $sougou_kei["NUM_{$grade}M"] = (int)$naika_kei["NUM_{$grade}M"] + (int)$geka_kei["NUM_{$grade}M"] + (int)$katsudo_kei["NUM_{$grade}M"] + (int)$sonota_kei["NUM_{$grade}M"] + (int)$igai_kei["NUM_{$grade}M"];
                $sougou_kei["NUM_{$grade}F"] = (int)$naika_kei["NUM_{$grade}F"] + (int)$geka_kei["NUM_{$grade}F"] + (int)$katsudo_kei["NUM_{$grade}F"] + (int)$sonota_kei["NUM_{$grade}F"] + (int)$igai_kei["NUM_{$grade}F"];
                $sougou_kei["NUM_{$grade}T"] = (int)$naika_kei["NUM_{$grade}T"] + (int)$geka_kei["NUM_{$grade}T"] + (int)$katsudo_kei["NUM_{$grade}T"] + (int)$sonota_kei["NUM_{$grade}T"] + (int)$igai_kei["NUM_{$grade}T"];
            }
            $sougou_kei["NUM_{$kind}M"] = (int)$naika_kei["NUM_{$kind}M"] + (int)$geka_kei["NUM_{$kind}M"] + (int)$katsudo_kei["NUM_{$kind}M"] + (int)$sonota_kei["NUM_{$kind}M"] + (int)$igai_kei["NUM_{$kind}M"];
            $sougou_kei["NUM_{$kind}F"] = (int)$naika_kei["NUM_{$kind}F"] + (int)$geka_kei["NUM_{$kind}F"] + (int)$katsudo_kei["NUM_{$kind}F"] + (int)$sonota_kei["NUM_{$kind}F"] + (int)$igai_kei["NUM_{$kind}F"];
            $sougou_kei["NUM_{$kind}T"] = (int)$naika_kei["NUM_{$kind}T"] + (int)$geka_kei["NUM_{$kind}T"] + (int)$katsudo_kei["NUM_{$kind}T"] + (int)$sonota_kei["NUM_{$kind}T"] + (int)$igai_kei["NUM_{$kind}T"];
        }

        $contents .= ",総合計,";
        $contents .= implode($sougou_kei, ",") ."\n";

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse="症状別保健室利用人数集計表";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
