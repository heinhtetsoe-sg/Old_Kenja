<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja610cModel extends Model {
    var $field         = array();
    var $select_data   = array();
    var $opt_subclass  = array();
    var $tableName;
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $virus;
        
    /**
     * メンバ変数の初期化
     */
    function knja610cModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init() {

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd         = VARS::request("cmd");

        $this->field = array("YEAR"                =>  VARS::post("YEAR")         //年度
                            , "SCHOOL_KIND"        =>  VARS::post("SCHOOL_KIND")  //校種
                            , "SEMESTER"           =>  VARS::post("SEMESTER")     //学期
                            , "TESTKINDCD"         =>  VARS::post("TESTKINDCD")   //テストコード
                            , "APPDATE"            =>  VARS::post("APPDATE")      //日付
                        );

        $this->field["YEAR"] = $this->field["YEAR"] ? $this->field["YEAR"] : CTRL_YEAR;

        // 出力対象クラス
        $this->selectHrClass = array();
        if (VARS::post("SELECT_HR_CLASS")) {
            $this->selectHrClass = explode(',', VARS::post("SELECT_HR_CLASS"));
        }
        // ヘッダ出力
        unset($this->chk_header);
        if (VARS::post("chk_header")) {
            $this->chk_header = VARS::post("chk_header");
        }

    }

    /**
     * 変数をクリアします
     */
    function clean() {
    }

    //入力項目チェック
    function validateInput() {

        $result = true;
        //DB接続
        $db = Query::dbCheckOut();

        $query = knja610cQuery::getSemesterInfo($this->field["YEAR"], $this->field["SEMESTER"]);
        $semesterInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($this->field["APPDATE"] < str_replace("-", "/", $semesterInfo["SDATE"]) 
        || str_replace("-", "/", $semesterInfo["EDATE"]) < $this->field["APPDATE"]) {
            $this->setWarning("MSG913", "日付");
            $result = false;
        }

        Query::dbCheckIn($db);

        return $result;
    }

    /**
     * 
     */
    function getDownloadModel() {

        //入力チェック
        if (!$this->validateInput()) {
            return;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダの作成//
        $header = array();
        $header['GRADE_NAME']      = "学年";
        $header['HR_CLASS_NAME']   = "クラス";
        $header['ATTENDNO']        = "出席番号";
        $header['NAME']            = "氏名";

        $header['SUBCLASSCD']      = "科目CD";
        $header['SUBCLASSNAME']    = "科目名";
        $header['CHAIRCD']         = "講座CD";
        $header['CHAIRNAME']       = "講座名";
        $header['STAFFNAME']       = "講座担当";

        $header['SCORE_01']        = "素点";
        $header['SCORE_08']        = "評価";
        $header['SCORE_9908']      = "評価（仮）";
        $header['SCORE_9909']      = "評定";

        $header['CLASS_RANK']      = "クラス順位";
        $header['COURSE_RANK']     = "コース順位";
        $header['GRADE_RANK']      = "学年順位";

        $contents = "";
        if ($this->chk_header) {
            $contents .= implode($header, ",")."\n";
        }

        // データ出力有無
        $isData = false;

        // データの取得と成形
        $contentsData = array();
        $query = knja610cQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "", $row);

            // 講座取得
            $sep = "";
            $chair = "";
            $query = knja610cQuery::getChair($this, $row["CLASSCD"], $row["SCHOOL_KIND"], $row["CURRICULUM_CD"], $row["SUBCLASSCD"], $row["SCHREGNO"]);
            $chairRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $row['CHAIRCD']   = str_replace(",", ".", $chairRow["CHAIRCD"]);
            $row['CHAIRNAME'] = str_replace(",", ".", $chairRow["CHAIRNAME"]);
            // 職員取得
            $sep = "";
            $staff = "";
            $query = knja610cQuery::getStaff($this, $row["CHAIRCD"]);
            $staffRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $row['STAFFCD']   = str_replace(",", ".", $staffRow["STAFFCD"]);
            $row['STAFFNAME'] = str_replace(",", ".", $staffRow["STAFFNAME"]);

            $sep = "";
            foreach ($header as $field => $name) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $isData = true;
        }
        $result->free();

        //学期情報取得
        $query = knja610cQuery::getSemesterInfo($this->field["YEAR"], $this->field["SEMESTER"]);
        $semesterInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //テスト種別取得
        $query = knja610cQuery::getTestItem($this, $this->field["TESTKINDCD"]);
        $testKind = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);

        if ($isData){
            $year = $this->field["YEAR"];
            $semester = $semesterInfo["SEMESTERNAME"];
            $testKindName = $testKind["TESTKINDNAME"];
            $dataName = "{$year}年度{$semester}_{$testKindName}成績データ";
            $fileName = sprintf("%s.csv", $dataName);
            common::downloadContents($contents, $fileName, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }
}
?>
