<?php

require_once('for_php7.php');

class knjl230cModel extends Model {
    var $applicantdiv;
    var $cmd;
    var $examyear;
    var $isGojo;
    var $pre_testdiv;

    function init()
    {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->pre_testdiv  = VARS::post("PRE_TESTDIV");

        //学校コードを取得
        $schoolCode = knjl230cQuery::getNameMstZ010();
        $this->isGojo = $schoolCode == "30290053001" ? true : false;
    }

    function &getMainModel()
    {
    }

    function validateInput() {
        if (!strlen($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!strlen($this->pre_testdiv)) {
            $this->setWarning("MSG301", "( プレテスト区分 )");
            return false;
        }
        //DB接続
        $db = Query::dbCheckOut();
        //対象人数チェック
        $query  = "SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_PRE_DAT T1 ";
        $query .= " WHERE T1.entexamyear  = '{$this->examyear}'";
        $query .= "   AND T1.applicantdiv = '{$this->applicantdiv}'";
        $query .= "   AND T1.pre_testdiv  = '{$this->pre_testdiv}'";
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); //DB切断
            $this->setMessage("MSG303", "\\n対象者がいません。");
            return false;
        }
        //得点データチェック
        $query  = "SELECT COUNT(*) FROM ENTEXAM_SCORE_PRE_DAT T1 ";
        $query .= " WHERE T1.entexamyear  = '{$this->examyear}'";
        $query .= "   AND T1.applicantdiv = '{$this->applicantdiv}'";
        $query .= "   AND T1.pre_receptno IN ( ";
        $query .= "        SELECT W1.pre_receptno FROM ENTEXAM_APPLICANTBASE_PRE_DAT W1 ";
        $query .= "         WHERE W1.entexamyear  = '{$this->examyear}'";
        $query .= "           AND W1.applicantdiv = '{$this->applicantdiv}'";
        $query .= "           AND W1.pre_testdiv  = '{$this->pre_testdiv}'";
        $query .= "   ) ";
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); //DB切断
            $this->setMessage("MSG303", "\\n対象者がいません。");
            return false;
        }
        //満点マスタチェック
        $query  = "SELECT COUNT(*) FROM ENTEXAM_PERFECT_PRE_MST T1 ";
        $query .= " WHERE T1.entexamyear  = '{$this->examyear}'";
        $query .= "   AND T1.applicantdiv = '{$this->applicantdiv}'";
        $query .= "   AND T1.pre_exam_type IN ( ";
        $query .= "        SELECT DISTINCT W1.pre_exam_type FROM ENTEXAM_APPLICANTBASE_PRE_DAT W1 ";
        $query .= "         WHERE W1.entexamyear  = '{$this->examyear}'";
        $query .= "           AND W1.applicantdiv = '{$this->applicantdiv}'";
        $query .= "           AND W1.pre_testdiv  = '{$this->pre_testdiv}'";
        $query .= "   ) ";
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db); //DB切断
            $this->setMessage("MSG303", "\\n満点マスタを設定して下さい。");
            return false;
        }
        return true;
    }

    function &getExecModel()
    {
        if($this->validateInput()){
            $ret = knjl230cQuery::ExecuteQuery($this);
            if ($ret == DB_OK) {
                $this->setMessage("MSG202");
            } else if ($ret < 0) {
                $this->setWarning("MSG203", "\\nエラーが発生しました。しばらくして処理を再開してください。\\n( それでも解決しない場合は管理者へ連絡して下さい。)");
            } else {
                $this->setMessage("MSG303", "\\n対象者がいません。");
            }
        }
    }
}
?>
