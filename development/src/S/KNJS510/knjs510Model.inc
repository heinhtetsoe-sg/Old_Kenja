<?php

require_once('for_php7.php');

//定数定義
class knjs510Model extends Model {
    var $field      = array();
    var $cmd;
    var $seq;
  
    //メンバ変数の初期化
    function knjs510Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        if(VARS::get("PROGRAMID")) {
          unset( $this->seq);
        }

        $this->field = array("SEQ"                  =>  VARS::post("SEQ"),                  //SEQ
                             "HOLIDAY_DIV"          =>  VARS::post("HOLIDAY_DIV"),          //祝祭日区分
                             "HOLIDAY_MONTH"        =>  VARS::post("HOLIDAY_MONTH"),        //月
                             "HOLIDAY_DAY"          =>  VARS::post("HOLIDAY_DAY"),          //日
                             "HOLIDAY_WEEK_PERIOD"  =>  VARS::post("HOLIDAY_WEEK_PERIOD"),  //週
                             "HOLIDAY_WEEKDAY"      =>  VARS::post("HOLIDAY_WEEKDAY"),      //曜日
                             "HOLIDAY_NAME"         =>  VARS::post("HOLIDAY_NAME"),         //祝祭日名
                             "HOLIDAY_KIND"         =>  VARS::post("HOLIDAY_KIND"),         //公休日区分
                             "MARK1"                =>  VARS::post("MARK1"),                //必須マーク
                             "MARK2"                =>  VARS::post("MARK2")                 //必須マーク
                            );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SEQ")) {
            $this->seq = VARS::get("SEQ");
        } 

        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //必須入力チェック
        if ($this->field["HOLIDAY_DIV"] == "") {
            $this->setWarning("MSG301", "( 祝祭日区分 )");
            return false;
        }
        if ($this->field["HOLIDAY_MONTH"] == "") {
            $this->setWarning("MSG301", "( 月 )");
            return false;
        }
        if ($kind == "update" || $kind == "delete") {
            if ($this->seq == "") {
                $this->setWarning("MSG308");
                return false;
            }
        }
        if ($kind == "insert" || $kind == "update") {
            //必須チェック
            if ($this->field["HOLIDAY_DIV"] == "1") {
                if ($this->field["HOLIDAY_DAY"] == "") {
                    $this->setWarning("MSG304", "( 日 )");
                    return false;
                }
            } else if ($this->field["HOLIDAY_DIV"] == "2") {
                if ($this->field["HOLIDAY_WEEK_PERIOD"] == "") {
                    $this->setWarning("MSG304", "( 週 )");
                    return false;
                }
                if ($this->field["HOLIDAY_WEEKDAY"] == "") {
                    $this->setWarning("MSG304", "( 曜日 )");
                    return false;
                }
            }

            //文字数チェック
            if (strlen($this->field["HOLIDAY_NAME"]) > 75 ) {
                $this->setWarning("MSG914","(祝祭日名は75バイトまでです)");
                return false;  
            }
        }

        return true;
    }

    //追加用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
        }
        return;
    }
    //追加
    function &insert()
    {
        $seq = knjs510Query::getInsertQuery($this->field, $this);
        if (!isset($model->warning)){
            $this->setMessage("MSG201");
            $this->seq = $seq;
        }
        return;
    }

    //更新用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
        }
        return;
    }
    //更新
    function &update()
    {
        knjs510Query::getUpdateQuery($this->field, $this->seq, $this);
        if (!isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
        }
        return;
    }
    //削除
    function &delete()
    {
        if (!isset($this->seq)) {
            $this->setWarning("MSG308");
            return;
        }

        knjs510Query::getDeleteQuery($this->field, $this->seq);
        if (!isset($model->warning)){
            $this->setMessage("MSG201");
        }
        $this->field = array();
        unset($this->seq);

        return;
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        $this_year_check = $db->getOne(knjs510Query::getPubHolidayData(CTRL_YEAR));
        $last_year_check = $db->getOne(knjs510Query::getPubHolidayData(CTRL_YEAR-1));

        if($this_year_check) {
            $this->setWarning("MSG203","既にデータが存在します。");
            return false;
        } else if(!$last_year_check) {
            $this->setWarning("MSG303");
            return false;
        } else {
            $result    = $db->query(knjs510Query::getPubHolidayData(CTRL_YEAR-1));   //前年度のデータ取得
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $db->query(knjs510Query::copyInsert($row, $this));             //前年度のデータを今年度のデータとしてINSERT
            }
        }
        Query::dbCheckIn($db);
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
