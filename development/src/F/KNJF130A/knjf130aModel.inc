<?php

require_once('for_php7.php');

class knjf130aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    function knjf130aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SCHKIND" => VARS::post("SCHKIND"), //校種
                             "DBNAME"  => VARS::post("DBNAME")   //DB変更に備えて
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
        $title = "学年時間別来室者状況";
        $wrk_date = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),0);

        //学校名称2表示
        $schoolname2 = "";
        if (($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $this->Properties["use_prg_schoolkind"] == "1") {
            $schoolname2 = $db->getOne(knjf130aQuery::getSchoolName2($this)).",";
        }

        $contents = $wrk_nendo ."," .$schoolname2. $title .",," ."作成日：" .$wrk_date ."\n";

        //ヘッダの作成//
        $header = array("KUBUN"       => "学年",
                        "NAMECD2"     => "来室校時",
                        "NINZUU4"     => "性別（男子数）",
                        "NINZUU5"     => "性別（女子数）",
                        );

        $contents .= implode($header, ",") ."\n";

        //SQL文発行（来室校時名の取得）
        $query = knjf130aQuery::getSelectName("F700");
        $result = $db->query($query);
        $opt_name1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
        }
        //学年名称取得
        $query = knjf130aQuery::getGradeName();
        $result = $db->query($query);
        $grade = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $grade[$row["GRADE"]] = $row["GRADE_NAME1"];
        }
        //出力データの作成//
        //SQL文発行
        $query = knjf130aQuery::selectCsvQuery($this);
        $result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["GRADE"] = $grade[$row["GRADE"]];
            $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
            $contents .= implode($row, ",") ."\n";

        }

        $result->free();
        Query::dbCheckIn($db);

        $couse="学年時間別来室者状況";
        $filename = sprintf("%s.csv", $couse);
        common::downloadContents($contents, $filename, "SJIS-win");
        return true;
    }
}
?>
