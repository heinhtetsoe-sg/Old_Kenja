<?php

require_once('for_php7.php');

class knjl141mModel extends Model {
    var $cmd;
    var $examyear;
    var $outputdiv;
    var $selected_data;
    var $suc_coursecd;
    var $suc_majorcd;
    var $suc_coursecode;
    var $testdiv;
    var $schoolName;

    function knjl141mModel(){}

    function init() {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");
        $this->outputdiv     = VARS::post("OUTPUTDIV");
        $this->selected_data = VARS::post("SELECTED_DATA");

        list($this->suc_coursecd,
             $this->suc_majorcd,
             $this->suc_coursecode,
             $this->testdiv) = explode(":", VARS::post("GOUKAKU"));

        $db = Query::dbCheckOut();
        $query = knjl141mQuery::getSchoolName();
        $this->schoolName = $db->getOne($query); //学校名取得
        Query::dbCheckIn($db);
    }

    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $query = knjl141mQuery::getKamokuSuu();
        $kamokuSuu = $db->getOne($query);

        $header = array("入学年度",
                        "※受験番号",
                        "氏名",
                        "氏名かな",
                        "性別",
                        "課程コード",
                        "学科コード",
                        "コースコード",
                        );

        $query = knjl141mQuery::getKmaokuMei();
        $kamokuMei = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $kamokuMei[] = $row["NAME1"];
        }

        $contents = implode($header, ","). ",". implode($kamokuMei, ","). ",合計,". implode($kamokuMei, ","). ",合計,DUMMY\n";
        $dataFlg  = false;

        //SQL文発行
        $query  = knjl141mQuery::selectCvsQuery($this, $kamokuSuu);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        if ($dataFlg) {
            //CSV出力
            $filename = $this->examyear."_入学者クラス編成用CSV出力.csv";

            //ファイルダウンロード
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
    }
}
?>
