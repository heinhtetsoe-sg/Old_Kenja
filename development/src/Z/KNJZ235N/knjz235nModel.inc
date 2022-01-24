<?php

require_once('for_php7.php');

class knjz235nModel extends Model {
    var $field = array();
    var $field2 = array();
    var $cmd;
    var $pattern;
    var $frmPatern;

    function knjz235nModel() {}

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->field);
            unset($this->schoolKind);
        }
        
        $this->getPropertiesAll(); //プロパティファイルの読込み

        $db = Query::dbCheckOut();
        $proCnt  = 0;
        $proKind = "";
        $this->SchKindOpt = $schKindArr = array();
        $query = knjz235nQuery::getSchoolKind($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $schKindArr[$row["VALUE"]] = $row["VALUE"];

            if ($this->Properties["knjz235nPattern_".$row["VALUE"]]) {
                $this->SchKindOpt[] = array("label" => $row["LABEL"],
                                            "value" => $row["VALUE"]
                                            );

                $proCnt++;
                $proKind = $row["VALUE"];
            }
        }

        // 学期情報取得
        $this->semesterList = array();
        $query = knjz235nQuery::getSemesterAll();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->semesterList[] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($proCnt > 0) {
            $this->schoolKind = VARS::post("SCHOOL_KIND");
            $this->schoolKind = ($this->schoolKind != "") ? $this->schoolKind: $this->SchKindOpt[0]["value"];
        }

        if ($proCnt == 0) {
            $Pattern = $this->Properties["knjz235nPattern"];
            if(strpos($Pattern, 'KNJ') === 0){
                $this->pattern = substr($Pattern,3);
            }
        } else if ($proCnt > 1) {
            if(strpos($this->Properties["knjz235nPattern_".$this->schoolKind], 'KNJ') === 0){
                $this->pattern = substr($this->Properties["knjz235nPattern_".$this->schoolKind], 3);
            }
        } else if ($proCnt == 1) {
            if(strpos($this->Properties["knjz235nPattern_".$proKind], 'KNJ') === 0){
                $this->pattern = substr($this->Properties["knjz235nPattern_".$proKind], 3);
            }
        }

        //校種(佐賀は校種コンボ表示する)
        if ($this->Properties["knjz235nPattern"] == "KNJD429") {
            $this->pattern    = 'D429';
        }
        
        if ($this->pattern == 'D154N') {
            $this->field = array("SEQ051"       =>  VARS::post("SEQ051"),       //帳票パターン      1:A 2:B
                                 "SEQ054"       =>  VARS::post("SEQ054"),       //平均点            1:クラス 2:コース 3:学年
                                 "SEQ057"       =>  VARS::post("SEQ057"),       //留学中の授業日数  
                                 "SEQ058"       =>  VARS::post("SEQ058"),       //LHR欠課時数       
                                 "SEQ059"       =>  VARS::post("SEQ059"),       //行事欠課時数      
                                 "SEQ062"       =>  VARS::post("SEQ062"),       //担任項目名        1:担任 2:チューター
                                 "SEQ063"       =>  VARS::post("SEQ063"),       //返信欄            
                                 "SEQ064"       =>  VARS::post("SEQ064"),       //度数分布表        
                                 "DBNAME"       =>  VARS::post("DBNAME")
                            );

        } else {
            $this->field = array("SEQ001"       =>  VARS::post("SEQ001"),
                                 "SEQ002"       =>  VARS::post("SEQ002"),
                                 "SEQ003"       =>  VARS::post("SEQ003"),
                                 "SEQ004"       =>  VARS::post("SEQ004"),
                                 "SEQ005"       =>  VARS::post("SEQ005"),
                                 "SEQ0061"      =>  VARS::post("SEQ0061"),
                                 "SEQ0062"      =>  VARS::post("SEQ0062"),
                                 "SEQ0063"      =>  VARS::post("SEQ0063"),
                                 "SEQ0064"      =>  VARS::post("SEQ0064"),
                                 "SEQ007"       =>  VARS::post("SEQ007"),
                                 "SEQ008"       =>  VARS::post("SEQ008"),
                                 "SEQ009"       =>  VARS::post("SEQ009"),
                                 "SEQ010"       =>  VARS::post("SEQ010"),
                                 "SEQ011"       =>  VARS::post("SEQ011"),
                                 "SEQ012"       =>  VARS::post("SEQ012"),
                                 "SEQ013"       =>  VARS::post("SEQ013"),
                                 "SEQ014"       =>  VARS::post("SEQ014"),
                                 "SEQ0151"      =>  VARS::post("SEQ0151"),
                                 "SEQ0152"      =>  VARS::post("SEQ0152"),
                                 "SEQ016"       =>  VARS::post("SEQ016"),
                                 "SEQ017"       =>  VARS::post("SEQ017"),
                                 "SEQ018"       =>  VARS::post("SEQ018"),
                                 "SEQ019"       =>  VARS::post("SEQ019"),
                                 "SEQ020"       =>  VARS::post("SEQ020"),
                                 "SEQ021"       =>  VARS::post("SEQ021"),
                                 "SEQ022"       =>  VARS::post("SEQ022"),
                                 "DBNAME"       =>  VARS::post("DBNAME")
                            );
        }

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "changeKind") {
            unset($this->field);
        }
    }

    //入力項目チェック
    function validateInput() {
        //if ($this->pattern == 'D429') {
        //    //表題
        //    if (strlen($this->field["SEQ001"]) > 30) {
        //        $this->setWarning("MSG914", "(表題) 30バイトまで");
        //        return false;
        //    }
        //}
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz235nQuery::getUpdateQuery($this);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }

    //前年度からコピー
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        //データ件数
        $pre_year = CTRL_YEAR - 1;
        $cnt = $db->getOne(knjz235nQuery::checkExistData($this, $pre_year, "", "copy"));

        //前年度のデータがない場合、エラーを返す
        if ($cnt == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //コピー処理
        $result = $db->query(knjz235nQuery::checkExistData($this, $pre_year, "", "pre_data"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            knjz235nQuery::copyInsert($db, $this, $row);
        }
        unset($this->field);
        $this->setMessage("MSG201");

        Query::dbCheckIn($db);
    }
}
?>
