<?php

require_once('for_php7.php');

class knjf110aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    function knjf110aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init() {
        $this->field = array("SCHKIND" =>  VARS::post("SCHKIND"),  //校種
                             "DBNAME"  =>  VARS::post("DBNAME")    //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行 2004/01/26 add nakamoto
        $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
        $title = "保健室利用者傷病集計";
        $wrk_date = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),0);

        //学校名称2表示
        $schoolname2 = "";
        if (($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $this->Properties["use_prg_schoolkind"] == "1") {
            $schoolname2 = $db->getOne(knjf110aQuery::getSchoolName2($this)).",";
        }

        $contents = $wrk_nendo ."," .$schoolname2. $title .",," ."作成日：" .$wrk_date ."\n";

        //ヘッダの作成//
        $header = array("KUBUN"       => "区分",
                        "NAMECD2"     => "傷病名",
                        "NINZUU4"     => "4月",
                        "NINZUU5"     => "5月",
                        "NINZUU6"     => "6月",
                        "NINZUU7"     => "7月",
                        "NINZUU8"     => "8月",
                        "NINZUU9"     => "9月",
                        "NINZUU10"    => "10月",
                        "NINZUU11"    => "11月",
                        "NINZUU12"    => "12月",
                        "NINZUU1"     => "1月",
                        "NINZUU2"     => "2月",
                        "NINZUU3"     => "3月",
                        "NINZUU"      => "計",
                        );

        $contents .= implode($header, ",") ."\n";

        /**************/
        /* 内科的症状 */
        /**************/
        //SQL文発行（来室理由の取得）
        $query = knjf110aQuery::getSelectName("F200");
        $result = $db->query($query);
        $opt_name1 = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
        }
        //出力データの作成//
        //SQL文発行
        $query = knjf110aQuery::selectCsvQuery('内科的症状', "1", "F200", $this);
        $result = $db->query($query);
        $data = false;
        //月毎の小計（内科）
        $naika_kei = array();
        for($i=0;$i<13;$i++) $naika_kei[$i] = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
            $contents .= implode($row, ",") ."\n";
            //月毎の小計（内科）を計算
            $naika_kei[0]  = $naika_kei[0]  + (int)$row["NINZUU4"];
            $naika_kei[1]  = $naika_kei[1]  + (int)$row["NINZUU5"];
            $naika_kei[2]  = $naika_kei[2]  + (int)$row["NINZUU6"];
            $naika_kei[3]  = $naika_kei[3]  + (int)$row["NINZUU7"];
            $naika_kei[4]  = $naika_kei[4]  + (int)$row["NINZUU8"];
            $naika_kei[5]  = $naika_kei[5]  + (int)$row["NINZUU9"];
            $naika_kei[6]  = $naika_kei[6]  + (int)$row["NINZUU10"];
            $naika_kei[7]  = $naika_kei[7]  + (int)$row["NINZUU11"];
            $naika_kei[8]  = $naika_kei[8]  + (int)$row["NINZUU12"];
            $naika_kei[9]  = $naika_kei[9]  + (int)$row["NINZUU1"];
            $naika_kei[10] = $naika_kei[10] + (int)$row["NINZUU2"];
            $naika_kei[11] = $naika_kei[11] + (int)$row["NINZUU3"];
            $naika_kei[12] = $naika_kei[12] + (int)$row["NINZUU"];

            $data = true;
        }
        //月毎の小計（内科）の行を出力
        $contents .= "内科的症状" ."," ."内科合計" .",";
        $contents .= implode($naika_kei, ",") ."\n";

        /**************/
        /* 外科的症状 */
        /**************/
        //SQL文発行（来室理由の取得）
        $query = knjf110aQuery::getSelectName("F201");
        $result = $db->query($query);
        $opt_name1 = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
        }
        //出力データの作成//
        //SQL文発行
        $query = knjf110aQuery::selectCsvQuery('外科的症状', "2", "F201", $this);
        $result = $db->query($query);
        $data = false;
        //月毎の小計（外科）
        $geka_kei = array();
        for($i=0;$i<13;$i++) $geka_kei[$i] = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
            $contents .= implode($row, ",") ."\n";
            //月毎の小計（外科）を計算
            $geka_kei[0]  = $geka_kei[0]  + (int)$row["NINZUU4"];
            $geka_kei[1]  = $geka_kei[1]  + (int)$row["NINZUU5"];
            $geka_kei[2]  = $geka_kei[2]  + (int)$row["NINZUU6"];
            $geka_kei[3]  = $geka_kei[3]  + (int)$row["NINZUU7"];
            $geka_kei[4]  = $geka_kei[4]  + (int)$row["NINZUU8"];
            $geka_kei[5]  = $geka_kei[5]  + (int)$row["NINZUU9"];
            $geka_kei[6]  = $geka_kei[6]  + (int)$row["NINZUU10"];
            $geka_kei[7]  = $geka_kei[7]  + (int)$row["NINZUU11"];
            $geka_kei[8]  = $geka_kei[8]  + (int)$row["NINZUU12"];
            $geka_kei[9]  = $geka_kei[9]  + (int)$row["NINZUU1"];
            $geka_kei[10] = $geka_kei[10] + (int)$row["NINZUU2"];
            $geka_kei[11] = $geka_kei[11] + (int)$row["NINZUU3"];
            $geka_kei[12] = $geka_kei[12] + (int)$row["NINZUU"];

            $data = true;
        }
        //月毎の小計（外科）の行を出力
        $contents .= "外科的症状" ."," ."外科合計" .",";
        $contents .= implode($geka_kei, ",") ."\n";

        /**********************/
        /* 健康相談活動症状 */
        /**********************/
        //SQL文発行（来室理由の取得）
        $query = knjf110aQuery::getSelectName("F219");
        $result = $db->query($query);
        $opt_name1 = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
        }
        //出力データの作成//
        //SQL文発行
        $query = knjf110aQuery::selectCsvQuery('健康相談活動症状', "5", "F219", $this);
        $result = $db->query($query);
        $data = false;
        //月毎の小計（健康相談活動）
        $soudan_kei = array();
        for($i=0;$i<13;$i++) $soudan_kei[$i] = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
            $contents .= implode($row, ",") ."\n";
            //月毎の小計（健康相談活動）を計算
            $soudan_kei[0]  = $soudan_kei[0]  + (int)$row["NINZUU4"];
            $soudan_kei[1]  = $soudan_kei[1]  + (int)$row["NINZUU5"];
            $soudan_kei[2]  = $soudan_kei[2]  + (int)$row["NINZUU6"];
            $soudan_kei[3]  = $soudan_kei[3]  + (int)$row["NINZUU7"];
            $soudan_kei[4]  = $soudan_kei[4]  + (int)$row["NINZUU8"];
            $soudan_kei[5]  = $soudan_kei[5]  + (int)$row["NINZUU9"];
            $soudan_kei[6]  = $soudan_kei[6]  + (int)$row["NINZUU10"];
            $soudan_kei[7]  = $soudan_kei[7]  + (int)$row["NINZUU11"];
            $soudan_kei[8]  = $soudan_kei[8]  + (int)$row["NINZUU12"];
            $soudan_kei[9]  = $soudan_kei[9]  + (int)$row["NINZUU1"];
            $soudan_kei[10] = $soudan_kei[10] + (int)$row["NINZUU2"];
            $soudan_kei[11] = $soudan_kei[11] + (int)$row["NINZUU3"];
            $soudan_kei[12] = $soudan_kei[12] + (int)$row["NINZUU"];

            $data = true;
        }
        //月毎の小計（健康相談活動）の行を出力
        $contents .= "健康相談活動症状" ."," ."健康相談活動合計" .",";
        $contents .= implode($soudan_kei, ",") ."\n";

        /**************/
        /* その他症状 */
        /**************/
        //SQL文発行（来室理由の取得）
        $query = knjf110aQuery::getSelectName("F203");
        $result = $db->query($query);
        $opt_name1 = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
        }
        //出力データの作成//
        //SQL文発行
        $query = knjf110aQuery::selectCsvQuery('その他症状', "3", "F203", $this);
        $result = $db->query($query);
        $data = false;
        //月毎の小計（その他）
        $sonota_kei = array();
        for($i=0;$i<13;$i++) $sonota_kei[$i] = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
            $contents .= implode($row, ",") ."\n";
            //月毎の小計（その他）を計算
            $sonota_kei[0]  = $sonota_kei[0]  + (int)$row["NINZUU4"];
            $sonota_kei[1]  = $sonota_kei[1]  + (int)$row["NINZUU5"];
            $sonota_kei[2]  = $sonota_kei[2]  + (int)$row["NINZUU6"];
            $sonota_kei[3]  = $sonota_kei[3]  + (int)$row["NINZUU7"];
            $sonota_kei[4]  = $sonota_kei[4]  + (int)$row["NINZUU8"];
            $sonota_kei[5]  = $sonota_kei[5]  + (int)$row["NINZUU9"];
            $sonota_kei[6]  = $sonota_kei[6]  + (int)$row["NINZUU10"];
            $sonota_kei[7]  = $sonota_kei[7]  + (int)$row["NINZUU11"];
            $sonota_kei[8]  = $sonota_kei[8]  + (int)$row["NINZUU12"];
            $sonota_kei[9]  = $sonota_kei[9]  + (int)$row["NINZUU1"];
            $sonota_kei[10] = $sonota_kei[10] + (int)$row["NINZUU2"];
            $sonota_kei[11] = $sonota_kei[11] + (int)$row["NINZUU3"];
            $sonota_kei[12] = $sonota_kei[12] + (int)$row["NINZUU"];

            $data = true;
        }
        //月毎の小計（その他）の行を出力
        $contents .= "その他症状" ."," ."その他合計" .",";
        $contents .= implode($sonota_kei, ",") ."\n";

        /****************/
        /* 生徒以外症状 */
        /****************/
        //SQL文発行（来室理由の取得）
        $query = knjf110aQuery::getSelectName("F202");
        $result = $db->query($query);
        $opt_name1 = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
        }

        //生徒項目名切替処理
        $sch_label = "";
        //テーブルの有無チェック
        $query = knjf110aQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && (($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $this->Properties["use_prg_schoolkind"] == "1")) {
            //生徒項目名取得
            $sch_label = $db->getOne(knjf110aQuery::getSchName($this));
        }
        $sch_label = (strlen($sch_label) > 0) ? $sch_label : '生徒';

        //出力データの作成//
        //SQL文発行
        $query = knjf110aQuery::selectCsvQuery($sch_label.'以外症状', "4", "F202", $this);
        $result = $db->query($query);
        $data = false;
        //月毎の小計（生徒以外）
        $igai_kei = array();
        for($i=0;$i<13;$i++) $igai_kei[$i] = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
            $contents .= implode($row, ",") ."\n";
            //月毎の小計（生徒以外）を計算
            $igai_kei[0]  = $igai_kei[0]  + (int)$row["NINZUU4"];
            $igai_kei[1]  = $igai_kei[1]  + (int)$row["NINZUU5"];
            $igai_kei[2]  = $igai_kei[2]  + (int)$row["NINZUU6"];
            $igai_kei[3]  = $igai_kei[3]  + (int)$row["NINZUU7"];
            $igai_kei[4]  = $igai_kei[4]  + (int)$row["NINZUU8"];
            $igai_kei[5]  = $igai_kei[5]  + (int)$row["NINZUU9"];
            $igai_kei[6]  = $igai_kei[6]  + (int)$row["NINZUU10"];
            $igai_kei[7]  = $igai_kei[7]  + (int)$row["NINZUU11"];
            $igai_kei[8]  = $igai_kei[8]  + (int)$row["NINZUU12"];
            $igai_kei[9]  = $igai_kei[9]  + (int)$row["NINZUU1"];
            $igai_kei[10] = $igai_kei[10] + (int)$row["NINZUU2"];
            $igai_kei[11] = $igai_kei[11] + (int)$row["NINZUU3"];
            $igai_kei[12] = $igai_kei[12] + (int)$row["NINZUU"];

            $data = true;
        }
        //月毎の小計（生徒以外）の行を出力
        $contents .= $sch_label."以外症状" ."," .$sch_label."以外合計" .",";
        $contents .= implode($igai_kei, ",") ."\n";

        //月毎の総合計の行を出力
        $sougou_kei = array();
        for($i=0;$i<13;$i++) $sougou_kei[$i] = (int)$geka_kei[$i] + (int)$naika_kei[$i] + (int)$soudan_kei[$i] + (int)$sonota_kei[$i] + (int)$igai_kei[$i];
        $contents .= "," ."総合計" .",";
        $contents .= implode($sougou_kei, ",") ."\n";

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse="保健室利用者傷病集計";
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
