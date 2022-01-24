<?php

require_once('for_php7.php');

class knjz200aModel extends Model {
    var $field = array();
    var $cmd;
    var $ibgrade_course;
    var $ibclasscd;
    var $ibcurriculum_cd;
    var $ibsubclasscd;
    var $replace = array();    //一括更新処理データ

    function knjz200aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->ibgrade_course);
            unset($this->ibclasscd);
            unset($this->ibprg_course);//GET用
            unset($this->ibcurriculum_cd);
            unset($this->ibsubclasscd);
            $this->replace = array();
        }

        list ($ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd) = explode('-', VARS::post("IBSUBCLASS"));

        $this->field = array("IBCLASSCD"        =>  $ibclasscd,
                             "IBPRG_COURSE"     =>  $ibprg_course,
                             "IBCURRICULUM_CD"  =>  $ibcurriculum_cd,
                             "IBSUBCLASSCD"     =>  $ibsubclasscd,
                             "NEED_TIME"        =>  VARS::post("NEED_TIME"),
                             );

        $this->cmd = VARS::request("cmd");

        $db = Query::dbCheckOut();
        //学年（初期値）
        $this->ibgrade_course = ($this->ibgrade_course == "") ? $db->getOne(knjz200aQuery::getIBGrade($this, "list")) : $this->ibgrade_course;
        
        Query::dbCheckIn($db);

        if (VARS::post("IBGRADE_COURSE") != "") {
            $this->ibgrade_course = VARS::post("IBGRADE_COURSE");
        }

        //左フレームからのパラメータ
        if(VARS::get("IBCLASSCD")) {
            $this->ibclasscd = VARS::get("IBCLASSCD");
        }
        if(VARS::get("IBPRG_COURSE")) {
            $this->ibprg_course = VARS::get("IBPRG_COURSE");
        }
        if(VARS::get("IBCURRICULUM_CD")) {
            $this->ibcurriculum_cd = VARS::get("IBCURRICULUM_CD");
        }
        if(VARS::get("IBSUBCLASSCD")) {
            $this->ibsubclasscd = VARS::get("IBSUBCLASSCD");
        }
        
        //一括処理
        if ($this->cmd == "replace_update") {
            $this->replace["selectdata_course"]     = VARS::post("selectdata_course");
            $this->replace["selectdata_subclass"]   = VARS::post("selectdata_subclass");
            $this->replace["field"] = $this->replace["data_chk"] = array();

            $this->replace["data_chk"] = array(VARS::post("RCHECK0"));
            $this->replace["check_all"] = VARS::post("RCHECK1");

            if (VARS::post("RCHECK0")=="1") $this->replace["field"]["NEED_TIME"] = VARS::post("NEED_TIME");
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (trim($this->ibgrade_course) == "") {
            $this->setWarning("MSG301", "(学年)");
            return false;
        }
        if (trim($this->field["IBCLASSCD"]) == "" || trim($this->field["IBPRG_COURSE"]) == "" || trim($this->field["IBCURRICULUM_CD"]) == "" || trim($this->field["IBSUBCLASSCD"]) == "") {
            $this->setWarning("MSG301", "(科目)");
            return false;
        }
        
        if ($this->cmd == "update" || $this->cmd == "delete") {
            list($grade, $course) = explode('-', $this->ibgrade_course);
            
            if ($this->cmd != "delete") {
                //必須チェック
                if (trim($this->field["NEED_TIME"]) == "") {
                    $this->setWarning("MSG301", "(必要時間)");
                    return false;
                }

                //文字数チェック
                if (strlen($this->field["NEED_TIME"]) > 5) {
                    $this->setWarning("MSG915","(必要時間は5バイトまでです)");
                    return false;
                }
            }
            //キーの更新チェック
            if ($this->ibclasscd != $this->field["IBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibprg_course != $this->field["IBPRG_COURSE"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibcurriculum_cd != $this->field["IBCURRICULUM_CD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibsubclasscd != $this->field["IBSUBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if (!strlen($this->ibclasscd) || !strlen($this->ibprg_course) || !strlen($this->ibcurriculum_cd) || !strlen($this->ibsubclasscd)) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz200aQuery::getRow($this->ibgrade_course, $this->field["IBCLASSCD"], $this->field["IBPRG_COURSE"], $this->field["IBCURRICULUM_CD"], $this->field["IBSUBCLASSCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //対象がなければエラー
        if ($this->cmd == "delete" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        } 
        //追加時に重複していたらエラー
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG302");
            return false;
        }

        return true;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            //登録
            knjz200aQuery::getInsertQuery($this);
            $this->setMessage("MSG201");

            $this->ibclasscd        = $this->field["IBCLASSCD"];
            $this->ibprg_course     = $this->field["IBPRG_COURSE"];
            $this->ibcurriculum_cd  = $this->field["IBCURRICULUM_CD"];
            $this->ibsubclasscd     = $this->field["IBSUBCLASSCD"];
        }
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        if ($this->validateInput()){
            //登録
            knjz200aQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()) {
            //削除
            knjz200aQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");

            unset($this->ibclasscd);
            unset($this->ibprg_course);
            unset($this->ibcurriculum_cd);
            unset($this->ibsubclasscd);
            unset($this->field);
        }
    }

    /********/
    /* COPY */
    /********/
    function &getCopyModel() {
        $db = Query::dbCheckOut();

        //存在チェック（前年度）
        $pre_year = CTRL_YEAR - 1;
        $cnt_pre = $db->getOne(knjz200aQuery::checkIBCreditMst($pre_year));
        if ($cnt_pre == 0) {
            $this->setWarning("MSG917","前年度のデータが存在しません。");
            return false;
        }
        //存在チェック（対象年度）
        $cnt_this = $db->getOne(knjz200aQuery::checkIBCreditMst(CTRL_YEAR));
        if ($cnt_this > 0) {
            $this->setWarning("MSG917","対象年度にデータが存在します。");
            return false;
        }

        Query::dbCheckIn($db);

        //コピー
        knjz200aQuery::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }
    

    //一括処理
    function &ReplaceModel()
    {
        //学年が選択されてない場合
        if(!strlen($this->replace["selectdata_course"])) {
            $this->setWarning("MSG304","　　（学年）");
            return false;
        }

        //IB科目が選択されてない場合
        if(!strlen($this->replace["selectdata_subclass"])) {
            $this->setWarning("MSG304","　　（IB科目）");
            return false;
        }

        //更新
        $this->rep_update();
        $this->setMessage("MSG201");

        //添え字を０から順序良くする
        $this->replace["check"] = $this->replace["data_chk"];
        $this->replace["check"][] = $this->replace["check_all"];
        return;
    }

    //IB単位マスタの一括更新
    function &rep_update()
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $grade_course = explode(",", $this->replace["selectdata_course"]);
        $subclass = explode(",", $this->replace["selectdata_subclass"]);

        for ($i = 0; $i < get_count($grade_course); $i++)
        {
            for ($j = 0; $j < get_count($subclass); $j++)
            {
                $query = knjz200aQuery::getRepUpdateQuery($db, $this, $this->replace["field"], $grade_course[$i], $subclass[$j]);
                $db->query($query);
            }
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
    
}
?>
