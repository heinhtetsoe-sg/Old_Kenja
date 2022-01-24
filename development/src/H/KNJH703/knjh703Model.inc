<?php

require_once('for_php7.php');

class knjh703Model extends Model
{
    public $fields = array();
    public $cmd;
    public $grade_hr_class;
    public $bunridiv;
    public $data_cnt;
    public $control;
    public $totalFields = array();

    public function knjh703Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->data_cnt = VARS::post("DATA_CNT");

        //更新中、サブミットする項目使用不可
        //よって、サブミットする項目の値は、hiddenから取得
        if ($this->cmd == 'update') {
            $this->grade_hr_class = VARS::post("H_GRADE_HR_CLASS");
        } else {
            $this->grade_hr_class = VARS::request("GRADE_HR_CLASS");
        }

        if (VARS::post("BUNRIDIV") == "") {
            $this->bunridiv = 1;
        } else {
            $this->bunridiv = VARS::post("BUNRIDIV");
        }

        //初期化
        $this->fields = array();

        if ($this->cmd == "update") {
            for ($g = 0; $g < $this->data_cnt; $g++) {
                $this->fields["SCHREGNO"][]    = VARS::post("SCHREGNO"."-".$g);
                $this->fields["BUNRIDIV"][]    = VARS::post("BUNRIDIV"."-".$g);
                $this->fields["SUBCLASSCD"][]  = VARS::post("SUBCLASSCD"."-".$g);
                $this->fields["DECLINE_FLG"][] = VARS::post("DECLINE_FLG"."-".$g);
            }
        } else if ($this->cmd == "bunri") {
            //文理区分変更時
            for ($g = 0; $g < $this->data_cnt; $g++) {
                $this->fields["SCHREGNO"][]    = VARS::post("SCHREGNO"."-".$g);
                $this->fields["GRADE"][]       = VARS::post("GRADE"."-".$g);
                $this->fields["HR_CLASS"][]    = VARS::post("HR_CLASS"."-".$g);
                $this->fields["ATTENDNO"][]    = VARS::post("ATTENDNO"."-".$g);
                $this->fields["NAME"][]        = VARS::post("NAME"."-".$g);
                $this->fields["SEX"][]         = VARS::post("SEX"."-".$g);
                $this->fields["BUNRIDIV"][]    = VARS::post("H_BUNRIDIV"."-".$g);
                $this->fields["SUBCLASSCD"][]  = VARS::post("H_SUBCLASSCD"."-".$g);
                $this->fields["DECLINE_FLG"][] = VARS::post("H_DECLINE_FLG"."-".$g);
            }
        } else if ($this->cmd == "replace_update") {
            //一括更新処理処理
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["BUNRIDIV"]    = VARS::post("BUNRIDIV");
            $this->replace_data["SUBCLASSCD"]  = VARS::post("SUBCLASSCD");
            $this->replace_data["DECLINE_FLG"] = VARS::post("DECLINE_FLG");
        } else if ($this->cmd == "replace_bunri") {
            //一括更新処理処理(文理区分変更時)
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["BUNRIDIV"]    = VARS::post("H_BUNRIDIV");
            $this->replace_data["SUBCLASSCD"]  = VARS::post("H_SUBCLASSCD");
            $this->replace_data["DECLINE_FLG"] = VARS::post("H_DECLINE_FLG");
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //更新処理
    public function getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $subclass = array();

        for ($row = 0; $row < $this->data_cnt; $row++) {
            $schregno   = $this->fields["SCHREGNO"][$row];
            $bunridiv   = $this->fields["BUNRIDIV"][$row];
            $subclasscd = explode("-", $this->fields["SUBCLASSCD"][$row]);

            $query = knjh703Query::checkExistData($schregno);
            if (0 < $db->getOne($query)) {
                //データがある場合は削除
                $db->query(knjh703Query::deleteSchregAcademictestDat($schregno));
            }
            //登録
            $db->query(knjh703Query::updateSchregAcademictestDat($this, $row, $subclasscd[0], $subclasscd[1]));

            //必須科目も登録する
            $query  = knjh703Query::getMustClass($bunridiv);
            $result = $db->query($query);
            while ($mustRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //必須科目登録
                $db->query(knjh703Query::updateSchregAcademictestDat($this, $row, $mustRow["CLASSCD"], $mustRow["SUBCLASSCD"]));
            }
            $result->free();
        }
        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

    //一括更新処理
    public function replaceModel()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        $this->replaceAcademictest();
        $this->setMessage("MSG201");
    }

    //一括更新
    public function replaceAcademictest()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $schregno   = explode(",", $this->replace_data["selectdata"]);
        $subclasscd = explode("-", $this->replace_data["SUBCLASSCD"]);
        for ($i = 0; $i < get_count($schregno); $i++) {
            $query = knjh703Query::checkExistData($schregno[$i]);
            if (0 < $db->getOne($query)) {
                //データがある場合は削除
                $db->query(knjh703Query::deleteSchregAcademictestDat($schregno[$i]));
            }
            //選択科目を選択している場合、登録
            if ($this->replace_data["SUBCLASSCD"] != "") {
                $db->query(knjh703Query::replaceSchregAcademictestDat($this, $schregno[$i], $subclasscd[0], $subclasscd[1]));
            }

            //必須科目も登録する
            $query  = knjh703Query::getMustClass($this->replace_data["BUNRIDIV"]);
            $result = $db->query($query);
            while ($mustRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //必須科目登録
                $db->query(knjh703Query::replaceSchregAcademictestDat($this, $schregno[$i], $mustRow["CLASSCD"], $mustRow["SUBCLASSCD"]));
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
}
