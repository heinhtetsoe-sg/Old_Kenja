<?php

require_once('for_php7.php');


class knje063Model extends Model {

    var $field        = array();
    var $cmd;
    var $schregno;
    var $class_select;
    var $subclasscd;
    var $sort;
    var $year = array();
    var $knje030cd;        //primary
    var $knje030year;
    var $knje030schreg;

    function knje063Model()
    {
        $this->sort["YEAR"] = 1;
        $this->year[0] = CTRL_YEAR;
    }

    function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();

        if(VARS::get("PROGRAMID")){
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            $this->year = array();
        }
        $this->grade_range = 3;
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "ANNUAL"           =>  VARS::post("ANNUAL"),           //学年(年次)
                             "CURRICULUM_CD"    =>  VARS::post("CURRICULUM_CD"),    //教育課程
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //科目コード
                             "CLASSNAME"        =>  VARS::post("CLASSNAME"),        //教科名
                             "CLASSABBV"        =>  VARS::post("CLASSABBV"),        //教科略略称
                             "CLASSNAME_ENG"    =>  VARS::post("CLASSNAME_ENG"),    //教科英
                             "CLASSABBV_ENG"    =>  VARS::post("CLASSABBV_ENG"),    //教科英略称
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目コード
                             "SUBCLASSNAME"     =>  VARS::post("SUBCLASSNAME"),     //科目名
                             "SUBCLASSABBV"     =>  VARS::post("SUBCLASSABBV"),     //科目略称
                             "SUBCLASSNAME_ENG" =>  VARS::post("SUBCLASSNAME_ENG"), //科目英
                             "SUBCLASSABBV_ENG" =>  VARS::post("SUBCLASSABBV_ENG"), //科目英略称
                             "REMARK"           =>  VARS::post("REMARK")            //備考
                             );                
        $this->subclasscd = $this->field["CLASSCD"].$this->field["SUBCLASSCD"];

        for ($i = 0; $i < $this->grade_range; $i++) {    
            $this->field[$i]["CHECKED1"]    = VARS::post("CHECKED1".$i);
            $this->field[$i]["VALUATION"]   = VARS::post("VALUATION".$i);
            $this->field[$i]["GET_CREDIT"]  = VARS::post("GET_CREDIT".$i);
            $this->field[$i]["ADD_CREDIT"]  = VARS::post("ADD_CREDIT".$i);
            $this->field[$i]["COMP_CREDIT"] = VARS::post("COMP_CREDIT".$i);
        }

        $this->checked  = VARS::request("CHECKED");          //チェック
        $this->checked2 = VARS::request("CHECKED2");          //チェック
        $this->cmd          = VARS::request("cmd");
        if ($this->cmd == "edit" && is_string($this->checked)){
            $a = explode(",", $this->checked);
            $this->editYear    = $a[0];
            $this->annual      = $a[1];
            $this->subclasscd  = $a[2];
        }else if ($this->cmd == "right" ){
            $this->editYear    = NULL;
            $this->annual      = NULL;
            $this->subclasscd  = NULL;
        }

        $this->knje030cd      = VARS::request("SUBCLASSCD");
        $this->knje030schreg  = VARS::request("SCHREGNO");
        $this->knje030year    = VARS::request("YEAR");

        //教科コンボより
        if (VARS::request("CLASSCD") =="") {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $classcd = substr($this->subclasscd,0,-4);
                $setClassSelect = preg_split("/-/", $classcd);
                $classcd = $setClassSelect[0]."-".$setClassSelect[1]."-".$setClassSelect[3];
                $this->field["CURRICULUM_CD"] = $setClassSelect[2];
            } else {
                $classcd = substr($this->subclasscd,0,2);
            }
        } else {
            $classcd = VARS::request("CLASSCD");
        }

        $this->class_select  = $classcd;

        if (VARS::request("SCHREGNO")){
            $this->schregno   = VARS::request("SCHREGNO");
        }
        if(VARS::get("NAME")){
            $this->name = VARS::get("NAME");
            $this->year = array(); //左のリストから選択されたら初期化
        }
        if(VARS::get("GRADE")){
            $this->grade = VARS::get("GRADE");
        }
        if(VARS::get("HR_CLASS")){
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if(VARS::get("ATTENDNO")){
            $this->attendno = VARS::get("ATTENDNO");
        }
        if(VARS::get("init") == 1){
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            $this->year = array();
        }
        if (VARS::post("YEAR")!="") {
            $this->year[] = VARS::post("YEAR");
        }
    }
    /**
     * 全体表示用モデルを返します
    */
    function &getMainModel()
    {
        //ソート用 番号、席次
        $sort = array("YEAR","SUBCLASSCD");
        //ソートが押下
        if (trim(VARS::request("sort")) != ""){
            foreach($sort as $field){
                if (VARS::request("sort") == $field
                        && is_numeric($this->sort[VARS::get("sort")])
                        && $this->sort[VARS::get("sort")] != 0) {
                    //昇順⇔降順
                    $this->sort[$field] = $this->sort[$field] *(-1);
                }elseif (VARS::get("sort") != $field) {
                    unset($this->sort[$field]);
                }else{
                    //昇順
                    $this->sort[$field] = 1;
                }
            }
        }
    }
    //入力項目チェック
    function validateInput(&$db)
    {
        if ($this->cmd == "update" ) {
            if ($this->schregno=="") {
                $this->setWarning("MSG304","(左より生徒を選択してから行ってください)");
                return false;
            }

            if (!is_numeric($this->field["YEAR"])){
                $this->setWarning("MSG301");
                return false;
            }

            //署名チェック
            if ($this->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
                $query = knje063Query::getOpinionsWk($this->field["YEAR"], $this->schregno);
                $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($check["CHAGE_OPI_SEQ"]) {
                    $this->setWarning("MSG917","（署名済み）");
                    return false;
                }
            }

            if (!is_numeric($this->field["ANNUAL"])){
                $this->setWarning("MSG301");
                return false;
            }
            if ($this->field["YEAR"] == "0" && sprintf("%02d", $this->field["ANNUAL"]) != "00" ||
                $this->field["YEAR"] != "0" && sprintf("%02d", $this->field["ANNUAL"]) == "00"){
                $this->setWarning("MSG915","0年度の年次は、00のみです。");
                return false;
            }
            if (is_numeric($this->field["VALUATION"]) && ((int) $this->field["VALUATION"] < 1 || (int) $this->field["VALUATION"] > 5)){
                $this->setWarning("MSG915","(評定)");
                return false;
            }
            if (strlen($this->field["REMARK"]) > 150){
                $this->setWarning("MSG915","(備考)");
                return false;
            }

            for ($i = 0; $i < $this->grade_range; $i++) {
                if ($this->field[$i]["VALUATION"] == "" && $this->field[$i]["GET_CREDIT"] == "" && $this->field[$i]["ADD_CREDIT"] == "" && $this->field[$i]["COMP_CREDIT"] == "") {
                    if ($i == "2" && $this->field[$i]["CHECKED1"] == "1") {
                        $this->setWarning("高認試験は、項目の何れかを指定して下さい。");
                        return false;
                    }
                    $checkval++;
                } else {
                    if ($this->field[$i]["CHECKED1"] == "1" &&
                        $this->field["YEAR"] == "0" &&
                        sprintf("%02d", $this->field["ANNUAL"]) == "00" &&
                        $i != 1
                    ){
                        $this->setWarning("MSG916","0年度 0年次は、\\n在籍前のみの登録となります。");
                        return false;
                    }
                }
                if (is_numeric($this->field[$i]["VALUATION"]) && ((int) $this->field[$i]["VALUATION"] < 1 || (int) $this->field[$i]["VALUATION"] > 5)){
                    $this->setWarning("MSG915","(評定)");
                    return false;
                }
            }

        }

        return true;
    }

    //同じ年度に複数の年次又は、同年次の複数年度はデータエラー
    function errCheck($db)
    {

        $row = $db->getRow(knje063Query::SelectAnnualQuery($this));

        if (is_array($row)) {
            $this->setWarning("MSG302","同じ年度内で異なる年次の追加はできません。");
            return false;
        }

        $row = $db->getRow(knje063Query::selectSchoolMstQuery($this->field["YEAR"]), DB_FETCHMODE_ASSOC);

        if ($row["SCHOOLDIV"] == "0") {
            return true;
        }

        $row = $db->getRow(knje063Query::SelectAnnualQuery2($this));

        if (is_array($row)) {
            $this->setWarning("MSG302","同じ年次で異なる年度の追加はできません。");
            return false;
        }
        return true;
    }
    
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        if (!strlen($this->subclasscd)) {
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (!$this->errCheck($db)) {
            return;
        }

        //入力チェック
        if ($this->validateInput($db)){
            for ($i = 0; $i < $this->grade_range; $i++) {

                if ($this->field[$i]["CHECKED1"] == "1") {
                    $query = knje063Query::getDeleteQuery($this, $i);
                    $db->query($query);

                    $query = knje063Query::getInsertQuery($this, $this->field, $this->grade_range, $i, $db);
                    if ($query) {
                        $db->query($query);
                    }
                }
            }
            //備考削除
            $query = knje063Query::getDeleteQueryRemark($this);
            $db->query($query);
            if ($this->field["REMARK"] != "") {
                //備考登録
                $query = knje063Query::getInsertQueryRemark($this);
                $db->query($query);
            }
            $this->setMessage("MSG201");
            $db->commit(); // トランザクションをコミットする。
            $this->annual = $this->field["ANNUAL"];
            $this->editYear = $this->field["YEAR"];
            $this->subclasscd = $this->field["CLASSCD"].$this->field["SUBCLASSCD"];
        } else {
            $db->rollback();
        }
        Query::dbCheckIn($db);
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }
        $db = Query::dbCheckOut();
        if ($this->cmd == "delete2") {
            //署名チェック
            if ($this->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
                $query = knje063Query::getOpinionsWk($this->field["YEAR"], $this->schregno);
                $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($check["CHAGE_OPI_SEQ"]) {
                    $this->setWarning("MSG917","（署名済み）");
                    return false;
                }
            }
        }
        //１レコード取得
        $query = knje063Query::getDeleteQuery($this);
        $db->query($query);
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
        $this->field = array();
    }
}
?>
