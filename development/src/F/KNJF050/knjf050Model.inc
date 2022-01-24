<?php

require_once('for_php7.php');

class knjf050Model extends Model {
    var $cmd;
    var $fields;
    var $flg;

    function knjf050Model()
    {
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        if($this->cmd == "total"){
            unset($this->flg);
        }

        $this->fields["UPDATED"] = VARS::post("UPDATED");
        $this->fields["SCHKIND"] = VARS::post("SCHKIND");

        //DB接続
        $db = Query::dbCheckOut();

        for($x=1; $x<=2; $x++)
        {
            $result = $db->query(knjf050Query::getGrade($this));
            while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $grade = ltrim($Row["GRADE"],"0");
                for($i=1; $i<=3; $i++)
                {
                    for($y=1; $y<=3; $y++)
                    {
                        if($y == 1){
                            $this->fields["HEIGHT".$x][$grade][$i] = VARS::post("HEIGHT".$x.$grade.$i);
                        } elseif($y == 2){
                            $this->fields["WEIGHT".$x][$grade][$i] = VARS::post("WEIGHT".$x.$grade.$i);
                        } elseif($y == 3){
                            $this->fields["SITHEIGHT".$x][$grade][$i] = VARS::post("SITHEIGHT".$x.$grade.$i);
                        }
                    }
                }
            }
        }
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput(){

        //DB接続
        $db = Query::dbCheckOut();

        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT){
            $this->setWarning("MSG300");
            return false;
        }

        for($x=1; $x<=2; $x++)
        {
            $result = $db->query(knjf050Query::getGrade($this));
            while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $grade = ltrim($Row["GRADE"],"0");
                for($i=1; $i<=3; $i++)
                {
                    for($y=1; $y<=3; $y++)
                    {
                        if($y == 1){
                            $height = explode(".",$this->fields["HEIGHT".$x][$grade][$i]);
                            if(strlen($height[0])>3){
                                $this->setWarning("MSG901","(身長)");
                                return false;
                            } elseif(strlen($height[1])>1){
                                $this->setWarning("MSG901","(身長) 小数点第一位まで");
                                return false;
                            }
                        } elseif($y == 2){
                            $weight = explode(".",$this->fields["WEIGHT".$x][$grade][$i]);
                            if(strlen($weight[0])>3){
                                $this->setWarning("MSG901","(体重)");
                                return false;
                            } elseif(strlen($weight[1])>1){
                                $this->setWarning("MSG901","(体重) 小数点第一位まで");
                                return false;
                            }
                        } elseif($y == 3){
                             $sitheight = explode(".",$this->fields["SITHEIGHT".$x][$grade][$i]);
                            if(strlen($sitheight[0])>3){
                                $this->setWarning("MSG901","(座高)");
                                return false;
                            } elseif(strlen($sitheight[1])>1){
                                $this->setWarning("MSG901","(座高) 小数点第一位まで");
                                return false;
                            }
                        }
                    }
                }
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        if($this->validateInput()){
            if(knjf050Query::getDeleteQuery($this)){
                knjf050Query::getInsertQuery($this, $this->fields);
                $this->setMessage("MSG201");
            }
            return;
        }
    }
}
?>