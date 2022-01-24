<?php

require_once('for_php7.php');

class knjl140sModel extends Model {
    var $cmd;
    var $examyear;
    var $outputdiv;
    var $selected_data;
    var $suc_coursecd;
    var $suc_majorcd;
    var $suc_coursecode;
    var $testdiv;
    var $schoolName;

    function knjl140sModel(){}

    function init() {
        //最終学期を取得
        $rtnSem = knjl140sQuery::getSemesterLast();
        $this->examyear      = ($rtnSem == CTRL_SEMESTER) ? CTRL_YEAR + 1 : CTRL_YEAR;
        $this->cmd           = VARS::request("cmd");
        $this->outputdiv     = VARS::post("OUTPUTDIV");
        $this->selected_data = VARS::post("SELECTED_DATA");

        list($this->suc_coursecd,
             $this->suc_majorcd,
             $this->suc_coursecode,
             $this->testdiv) = explode(":", VARS::post("GOUKAKU"));

        $db = Query::dbCheckOut();
        $query = knjl140sQuery::getSchoolName();
        $this->schoolName = $db->getOne($query); //学校名取得
        Query::dbCheckIn($db);
    }

    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $subtitle = "";
        if ($this->outputdiv == 1) {
            $dataFlg  = $this->getDownloadStudent($db, $contents);
        } else {
            $dataFlg  = $this->getDownloadGuardian($db, $contents);
            $subtitle = "保護者";
        }

        if ($dataFlg) {
            //CSV出力
            $this->putCsv($db, $contents, $subtitle);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //CSV出力
    function putCsv($db, $contents, $subtitle) {
        $filename = $this->examyear."_入学者".$subtitle.".csv";

        Query::dbCheckIn($db);

        //ファイルダウンロード
        common::downloadContents($contents, $filename);
    }

    //合格者
    function getDownloadStudent($db, &$contents) {
        $header = array("入学年度",
                        "※学籍番号",
                        "※入学区分",
                        "※組",
                        "出席番号",
                        "※内外区分",
                        "課程コード",
                        "学科コード",
                        "コースコード",
                        "氏名",
                        "氏名かな",
                        "生年月日",
                        "性別",
                        "出身学校コード",
                        "出身学校卒業日",
                        "郵便番号",
                        "住所１",
                        "住所２",
                        "電話番号",
                        "FAX番号",
                        "EMAIL",
                        "緊急連絡先",
                        "緊急連絡電話番号",
                        "スカラシップ区分",
                        "課程入学年度",
                        "地区コード",
                        "受検番号",
                        "無償回数",
                        "DUMMY");

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        //SQL文発行
        $query  = knjl140sQuery::selectCvsStudentQuery($this);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["EXAMNO"] = substr($row["SCHREGNO"], 1); //受検番号は、下4桁を出力
            $row["SCHREGNO"] = $this->examyear .$row["EXAMNO"]; //年度（西暦）+ 受検番号下4桁
            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }

    //保護者
    function getDownloadGuardian($db, &$contents) {
        $header = array("学年",
                        "クラス",
                        "出席番号",
                        "氏名",
                        "※学籍番号",
                        "※続柄",
                        "保護者氏名",
                        "保護者氏名かな",
                        "性別",
                        "生年月日",
                        "郵便番号",
                        "住所1",
                        "住所2",
                        "電話番号",
                        "FAX番号",
                        "E-MAIL",
                        "職種コード",
                        "勤務先名称",
                        "勤務先電話番号",
                        "保証人続柄",
                        "保証人氏名",
                        "保証人氏名かな",
                        "保証人性別",
                        "保証人郵便番号",
                        "保証人住所1",
                        "保証人住所2",
                        "保証人電話番号",
                        "保証人職種コード",
                        "兼ねている公職",
                        "DUMMY");

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        $query  = knjl140sQuery::selectCvsGuardQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row["SCHREGNO"] = $this->examyear .substr($row["SCHREGNO"], 1); //年度（西暦）+ 受検番号下4桁
            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }
}
?>
