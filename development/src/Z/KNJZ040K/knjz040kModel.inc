<?php

require_once('for_php7.php');

//定数定義
class knjz040kModel extends Model {
    var $field = array();
    var $cmd;
    var $coursecd;          //過程コード
    var $majorcd;           //学科コード
    var $examcoursecd;      //コースコード
    var $shdiv;             //補完過程コード
    var $judgement;         //補完学科コード
    var $cmp_coursecd;      //補完コースコード
    var $cmp_majorcd;       //専併区分
    var $cmp_examcoursecd;  //判定
    var $year;
    var $NextYear;

    //メンバ変数の初期化
    function knjz040kModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->coursecd);
            unset($this->majorcd);
            unset($this->examcoursecd);
            unset($this->shdiv);
            unset($this->judgement);
            unset($this->cmp_coursecd);
            unset($this->cmp_majorcd);
            unset($this->cmp_examcoursecd);
        }

        $this->cmd = VARS::request("cmd");

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        if(VARS::get("COURSECD")) {
            $this->coursecd = VARS::get("COURSECD");
            $this->majorcd = VARS::get("MAJORCD");
            $this->examcoursecd = VARS::get("EXAMCOURSECD");
            $this->shdiv = VARS::get("SHDIV");
            $this->judgement = VARS::get("JUDGEMENT");
            $this->cmp_coursecd = VARS::get("CMP_COURSECD");
            $this->cmp_majorcd = VARS::get("CMP_MAJORCD");
            $this->cmp_examcoursecd = VARS::get("CMP_EXAMCOURSECD");
        }

        $this->field = array("YEAR"             => $this->year,
                             "TOTALCD"          => VARS::post("TOTALCD"),
                             "COURSECD"         => substr(VARS::post("TOTALCD"),0,1),       //過程コード
                             "MAJORCD"          => substr(VARS::post("TOTALCD"),1,3),       //学科コード
                             "EXAMCOURSECD"     => substr(VARS::post("TOTALCD"),4,4),       //コースコード
                             "SHDIV"            => VARS::post("SHDIV"),                     //専併区分
                             "JUDGEMENT"        => VARS::post("JUDGEMENT"),                 //判定
                             "CMP_TOTALCD"      => VARS::post("CMP_TOTALCD"),
                             "CMP_COURSECD"     => substr(VARS::post("CMP_TOTALCD"),0,1),   //補完過程コード
                             "CMP_MAJORCD"      => substr(VARS::post("CMP_TOTALCD"),1,3),   //補完学科コード
                             "CMP_EXAMCOURSECD" => substr(VARS::post("CMP_TOTALCD"),4,4),   //補完コースコード
                             "S_JUDGEMENT"      => VARS::post("S_JUDGEMENT"),               //専願判定
                             "H_JUDGEMENT"      => VARS::post("H_JUDGEMENT")                //併願判定
                             );
    }

    //入力項目チェック
    function validateInput($kind)
    {
        if($kind == "insert" || $kind == "update"){
            //空文字チェック
            if(strlen($this->field["COURSECD"]) == 0 || strlen($this->field["MAJORCD"]) == 0 ||
               strlen($this->field["EXAMCOURSECD"]) == 0) {
                $this->setWarning("MSG301","( コース )");
                return false;
            }
            if(strlen($this->field["SHDIV"]) == 0){
                $this->setWarning("MSG301","( 専併区分 )");
                return false;
            }
            if(strlen($this->field["JUDGEMENT"]) == 0){
                $this->setWarning("MSG301","( 事前判定 )");
                return false;
            }
            if(strlen($this->field["CMP_COURSECD"]) == 0 || strlen($this->field["CMP_MAJORCD"]) == 0 ||
               strlen($this->field["CMP_EXAMCOURSECD"]) == 0) {
                $this->setWarning("MSG301","( 補完コース )");
                return false;
            }
        }

        if(($kind=="update" || $kind=="delete") && ($this->coursecd != $this->field["COURSECD"] ||
                                                    $this->majorcd != $this->field["MAJORCD"] ||
                                                    $this->examcoursecd != $this->field["EXAMCOURSECD"] ||
                                                    $this->shdiv != $this->field["SHDIV"] ||
                                                    $this->judgement != $this->field["JUDGEMENT"] ||
                                                    $this->cmp_coursecd != $this->field["CMP_COURSECD"] ||
                                                    $this->cmp_majorcd != $this->field["CMP_MAJORCD"] ||
                                                    $this->cmp_examcoursecd != $this->field["CMP_EXAMCOURSECD"]
                                                    )){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        $row = knjz040kQuery::getRow($this,2);      //入力項目の１レコード取得

        //重複チェック
        if (($kind == "insert") && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $db->query(knjz040kQuery::getInsertQuery($this->field));
            $db->commit();
            Query::dbCheckIn($db);

            $this->coursecd = $this->field["COURSECD"];
            $this->majorcd = $this->field["MAJORCD"];
            $this->examcoursecd = $this->field["EXAMCOURSECD"];
            $this->shdiv = $this->field["SHDIV"];
            $this->judgement =$this->field["JUDGEMENT"];
            $this->cmp_coursecd = $this->field["CMP_COURSECD"];
            $this->cmp_majorcd = $this->field["CMP_MAJORCD"];
            $this->cmp_examcoursecd = $this->field["CMP_EXAMCOURSECD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $db->query(knjz040kQuery::getDeleteQuery($this->field));
            $db->query(knjz040kQuery::getInsertQuery($this->field));

            $db->commit();
            Query::dbCheckIn($db);

            $this->coursecd = $this->field["COURSECD"];
            $this->majorcd = $this->field["MAJORCD"];
            $this->examcoursecd = $this->field["EXAMCOURSECD"];
            $this->shdiv = $this->field["SHDIV"];
            $this->judgement =$this->field["JUDGEMENT"];
            $this->cmp_coursecd = $this->field["CMP_COURSECD"];
            $this->cmp_majorcd = $this->field["CMP_MAJORCD"];
            $this->cmp_examcoursecd = $this->field["CMP_EXAMCOURSECD"];
            $this->setMessage("MSG201");            
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $db->query(knjz040kQuery::getDeleteQuery($this->field));
            $db->commit();
            Query::dbCheckIn($db);

            $this->coursecd = "";
            $this->majorcd = "";
            $this->examcoursecd = "";
            $this->shdiv = "";
            $this->judgement = "";
            $this->cmp_coursecd = "";
            $this->cmp_majorcd = "";
            $this->cmp_examcoursecd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel()
    {
        if ($this->year == (CTRL_YEAR + 1)) {
            //今年度のデータが存在するかチェック
            $t_year = knjz040kQuery::CheckData($this->year);
            if(!isset($t_year)){
               $this->setWarning('MSG203', "今年度のデータが存在しません。");
               return;
            }
        }
        $this->NextYear = $this->year + 1;
        if(knjz040kQuery::getCopyYearQuery($this))
        {
            $this->year = $this->NextYear;
            $this->setMessage("MSG201");
        }else{
            $this->setMessage("MSG203", "\\n作成するデータが存在しませんでした。\\n既に作成されているか、" . "$this->NextYear" . "年度の受験コースマスタか\\n名称マスタが存在していません。");
        }

        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
