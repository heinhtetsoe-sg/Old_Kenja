<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl034bModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $judgment_div;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl034bModel()
    {
    }
    function init()
    {
        if(VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
            unset($this->judgment_div);
            $this->ObjYear = CTRL_YEAR+1;
        }
        $this->ObjYear = VARS::post("YEAR");
        $this->field = array("JUDGMENT_DIV"                 =>  VARS::post("JUDGMENT_DIV"),                 //合否詳細区分
                             "PROMISE_COURSE_NAME"          =>  VARS::post("PROMISE_COURSE_NAME"),          //確約区分の名称
                             "PROMISE_COURSE_ABBV"          =>  VARS::post("PROMISE_COURSE_ABBV"),          //確約区分の略称
                             "PROMISE_RECOMMEND_TEST_FLG"   =>  VARS::post("PROMISE_RECOMMEND_TEST_FLG"),   //推薦入試確約
                             "PROMISE_GENERAL_TEST_FLG"     =>  VARS::post("PROMISE_GENERAL_TEST_FLG"),     //一般入試確約
                             "JUDGMENT_COURSE_NAME"         =>  VARS::post("JUDGMENT_COURSE_NAME"),         //合否区分の名称
                             "JUDGMENT_COURSE_ABBV"         =>  VARS::post("JUDGMENT_COURSE_ABBV"),         //合否区分の略称
                             "TAKE_RECOMMEND_TEST_FLG"      =>  VARS::post("TAKE_RECOMMEND_TEST_FLG"),      //推薦入試(正規)
                             "TAKE_GENERAL_TEST_FLG"        =>  VARS::post("TAKE_GENERAL_TEST_FLG"),        //一般入試(正規)
                             "CHANGE_SINGLE_TEST_FLG"       =>  VARS::post("CHANGE_SINGLE_TEST_FLG"),       //一般入試(単願切換)
                             "NORMAL_PASSCOURSECD_NAME_SET" =>  VARS::post("NORMAL_PASSCOURSECD_NAME_SET"), //正規合格コース
                             "EARLY_PASSCOURSECD_NAME_SET"  =>  VARS::post("EARLY_PASSCOURSECD_NAME_SET"),  //単願切換合格コース
                             "PASSCOURSE_DIV"               =>  VARS::post("PASSCOURSE_DIV"),               //コース区分
                             "SCHOOLWORK_DIV"               =>  VARS::post("SCHOOLWORK_DIV"),               //学/体区分
                             "SPECIAL_DIV"                  =>  VARS::post("SPECIAL_DIV")                   //特待生区分
                     );

        $this->cmd         = VARS::request("cmd");
        
        if (VARS::get("JUDGMENT_DIV")!="") {
            $this->judgment_div  = VARS::get("JUDGMENT_DIV");
        }
        
    }
    
    //次年度のデータを作成
    function &getCopyYearModel()
    {
        $db = Query::dbCheckOut();

        $this->nextYear = $this->ObjYear + 1;
        $message = '';
        $flg = true;
        if (!checkThisYear($db, $this->ObjYear)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (!checkNextYear($db, $this->nextYear)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjl034bQuery::getCopyQuery($this->ObjYear, $this->nextYear);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }
        Query::dbCheckIn($db);
    }
    
    //入力項目チェック
    function validateInput()
    {
        //選択チェック
        $db = Query::dbCheckOut();
        $getCount = $db->getOne(knjl034bQuery::getCountdata($this));
        Query::dbCheckIn($db);
        
        if (AUTHORITY == DEF_UPDATABLE) {
            if ($this->judgment_div) {
                if ($getCount > 0) {
                    if ($this->field["JUDGMENT_DIV"] !== $this->judgment_div) {
                        $this->setWarning("MSG918", "一覧より選択してから編集を行って下さい。");
                        return false;
                    }
                }
            } else {
                if ($getCount > 0) {
                    $this->setWarning("MSG918", "一覧より選択してから編集を行って下さい。");
                }
            }
        } else {
            if (!$this->judgment_div) {
                $this->setWarning("MSG308");
            }
        }
        //必須チェック
        if (AUTHORITY == DEF_UPDATABLE) {
            if ($this->field["JUDGMENT_DIV"] == "") {
                $this->setWarning("MSG301","(合否詳細区分)");
                return false;
            }
        }
        if ($this->field["JUDGMENT_COURSE_NAME"] == "") {
            $this->setWarning("MSG301","(合否区分の名称)");
            return false;
        }
        if ($this->field["JUDGMENT_COURSE_ABBV"] == "") {
            $this->setWarning("MSG301","(合否区分の略称)");
            return false;
        }
        //バイトチェック
        if (strlen($this->field["JUDGMENT_DIV"]) > 2) {
            $this->setWarning("MSG915","合否詳細区分は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["JUDGMENT_COURSE_NAME"]) > 60) {
            $this->setWarning("MSG915","名称は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["PROMISE_COURSE_NAME"]) > 45) {
            $this->setWarning("MSG915","名称は45バイトまでです。");
            return false;
        }
        if (strlen($this->field["PROMISE_COURSE_ABBV"]) > 30 || strlen($this->field["JUDGMENT_COURSE_ABBV"]) > 30) {
            $this->setWarning("MSG915","略称は30バイトまでです。");
            return false;
        }
        //単願、正規チェック
        if (($this->field["TAKE_RECOMMEND_TEST_FLG"] != "" || $this->field["TAKE_GENERAL_TEST_FLG"] != "") && ($this->field["CHANGE_SINGLE_TEST_FLG"] != "")) {
            $this->setWarning("MSG901","各入試の正規と一般入試の単願切換は同時にチェックすることはできません。");
            return false;
        }
        if (($this->field["TAKE_RECOMMEND_TEST_FLG"] != "" || $this->field["TAKE_GENERAL_TEST_FLG"] != "") && ($this->field["NORMAL_PASSCOURSECD_NAME_SET"] == "")) {
            $this->setWarning("MSG901","各入試の正規にチェックしている時は、正規合格コースは必ず設定して下さい。");
            return false;
        }
        if (($this->field["CHANGE_SINGLE_TEST_FLG"] != "") && ($this->field["EARLY_PASSCOURSECD_NAME_SET"] == "")) {
            $this->setWarning("MSG901","一般入試の単願切換にチェックしている時は、単願切換合格コースは必ず設定して下さい。");
            return false;
        }
        
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl034bQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }
    
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}

//次年度コピーチェック用関数
function checkThisYear($db, $year) {
    $query = knjl034bQuery::getCheckCourseJudgmentMstSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear) {
    $query = knjl034bQuery::getCheckCourseJudgmentMstSql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
?>
