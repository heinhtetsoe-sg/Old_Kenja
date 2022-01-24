<?php

require_once('for_php7.php');

class knje062Model extends Model {

    var $field        = array();
    var $cmd;
    var $schregno;
    var $sort;
    var $year = array();

    function knje062Model()
    {
        $this->sort["YEAR"] = 1;
        $this->year[0] = CTRL_YEAR;
    }

    function init()
    {
        if(VARS::get("PROGRAMID")){
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            $this->year = array();
        }
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),             //年度
                             "ANNUAL"       =>  VARS::post("ANNUAL"),           //学年(年次)
                             "SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),       //科目
                             "CLASSNAME"    =>  VARS::post("CLASSNAME"),        //教科名
                             "VALUATION"    =>  VARS::post("VALUATION"),        //評定
                             "GET_CREDIT"   =>  VARS::post("GET_CREDIT"),       //修得単位
                             "ADD_CREDIT"   =>  VARS::post("ADD_CREDIT"),       //増加単位
                             "REMARK"       =>  VARS::post("REMARK")            //備考
                             );

        $this->checked = VARS::request("CHECKED");          //チェック
        $this->cmd          = VARS::request("cmd");
        if ($this->cmd == "edit" && is_string($this->checked)){
            $a = explode(",", $this->checked);
            $this->schoolcd    = $a[0];
            $this->annual      = $a[1];
            $this->subclasscd  = $a[2];
        }else if ($this->cmd == "right" ){
            $this->schoolcd    = NULL;
            $this->annual      = NULL;
            $this->subclasscd  = NULL;
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

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
        if ($this->cmd == "add" || $this->cmd == "update" ) {
            if ($this->schregno=="") {
                $this->setWarning("MSG304","(左より生徒を選択してから行ってください)");
                return false;
            }

            if (!is_numeric($this->field["YEAR"])){
                $this->setWarning("MSG301");
                return false;
            }
            if (!is_numeric($this->field["ANNUAL"])){
                $this->setWarning("MSG301");
                return false;
            }
            if (is_numeric($this->field["VALUATION"]) && ((int) $this->field["VALUATION"] < 1 || (int) $this->field["VALUATION"] > 5)){
                $this->setWarning("MSG915","(評定)");
                return false;
            }
            if (strlen($this->field["REMARK"]) > 60){
                $this->setWarning("MSG915","(備考)");
                return false;
            }
            $row = $db->getRow(knje062Query::selectQuery($this));
            if (is_array($row)){
                $this->setWarning("MSG302");
                return false;
            }
        }

        return true;
    }

    //同じ年度に複数の年次はデータはエラー
    function errCheck($db)
    {
        if ($this->cmd == "add" || ($this->cmd == "update" && $this->annual != $this->field["ANNUAL"])) {

            $result = $db->query(knje062Query::SelectAnnualQuery($this));

            if ($result->numRows() != "0") {
                $this->setWarning("MSG302","同じ年度内で異なる年次の追加はできません。");
                return false;
            }
            $result->free();
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
        
        $query = knje062Query::getDeleteQuery($this);
        $db->query($query);

        //入力チェック
        if ($this->validateInput($db)){
            $query = knje062Query::getInsertQuery($this);
            $db->query($query);
            //備考削除
            $query = knje062Query::getDeleteQueryRemark($this);
            $db->query($query);
            if ($this->field["REMARK"] != ""){
                //備考登録
                $query = knje062Query::getInsertQueryRemark($this);
                $db->query($query);
            }
            $this->setMessage("MSG201");
            $db->commit(); // トランザクションをコミットする。
            $this->annual = $this->field["ANNUAL"];
            $this->subclasscd = $this->field["SUBCLASSCD"];
        }else{
            $db->rollback();
        }
        Query::dbCheckIn($db);
    }

    function &getInsertModel()
    {
        //１レコード取得
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (!$this->errCheck($db)) {
            return;
        }
        //入力チェック
        if ($this->validateInput($db)){
            $query = knje062Query::getInsertQuery($this);
            $db->query($query);
            //備考削除
            $query = knje062Query::getDeleteQueryRemark($this);
            $db->query($query);
            if ($this->field["REMARK"] != ""){
                //備考登録
                $query = knje062Query::getInsertQueryRemark($this);
                $db->query($query);
            }
            $this->setMessage("MSG201");
            $this->annual = $this->field["ANNUAL"];
            $this->subclasscd = $this->field["SUBCLASSCD"];

        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }
        //１レコード取得
        $db = Query::dbCheckOut();
        $query = knje062Query::getDeleteQuery($this);
        $db->query($query);
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
        $this->field = array();
    }
}
?>
