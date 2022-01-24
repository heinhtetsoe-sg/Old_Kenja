<?php

require_once('for_php7.php');

//メニュー画面
class knjl030yModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $mode;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $exam_type;
    var $field = array();
    var $year;
    
    function knjl030yModel()
    {
    }        
    function init()
    {
        $this->year = CTRL_YEAR + 1;
        $this->cmd      = VARS::request("cmd");
        $this->exam_type = "1";

        if (VARS::get("PROGRAMID")){
            unset($this->examhallcd);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->mode);
        }

        if (VARS::get("examhallcd")){
            $this->examhallcd   = VARS::get("examhallcd");
        }
        if (VARS::post("APPLICANTDIV")){
            $this->applicantdiv = VARS::post("APPLICANTDIV");
        }
        if (VARS::post("APP_HOLD")){
            $this->appHold = VARS::post("APP_HOLD");
        }
        if (VARS::post("TESTDIV")){
            $this->testdiv   = VARS::post("TESTDIV");
        }
        if (VARS::request("mode")){
            $this->mode   = VARS::request("mode");
        }
        //受付データの合計人数を保持
        if (VARS::post("CAPA_CNT_RECEPT")){
            $this->capa_cnt_recept = VARS::post("CAPA_CNT_RECEPT");
        }
        //会場名
        $this->field["EXAMHALL_NAME"] = VARS::post("EXAMHALL_NAME");
        //分割数
        $this->field["DETAIL_CNT"] = VARS::post("DETAIL_CNT");
        //分割人数
        $detail_capa_cnt_sum = 0;
        if (strlen($this->field["DETAIL_CNT"]) && 0 < (int) $this->field["DETAIL_CNT"]) {
            for ($detail_no = 1; $detail_no <= (int) $this->field["DETAIL_CNT"]; $detail_no++) {
                $name = "DETAIL_CAPA_CNT" .$detail_no;
                $this->field[$name] = VARS::post($name);
                $detail_capa_cnt_sum += $this->field[$name];
            }
        }
        //会場人数
//        $this->field["CAPA_CNT"] = VARS::post("CAPA_CNT");
        $this->field["CAPA_CNT"] = $detail_capa_cnt_sum;
        $this->field["CHECKED"] = VARS::post("CHECKED");
        if ($this->mode == "insert"){
            unset($this->examhallcd);
        }
    }
    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {
    }

    //入力項目チェック
    function validateInput()
    {
        if (!strlen($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!strlen($this->testdiv)) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if ($this->cmd == "delete") {
            if (!is_array($this->field["CHECKED"])) {
                $this->warning = "チェックボックスが選択されていません。";
                return false;
            }
        } else if ($this->cmd == "insert" || $this->cmd == "update" ) {
            if (strlen($this->field["EXAMHALL_NAME"]) == 0 || 
                !is_numeric($this->field["CAPA_CNT"]) || $this->field["CAPA_CNT"] == 0 || 
                !is_numeric($this->field["DETAIL_CNT"]) || $this->field["DETAIL_CNT"] == 0) 
            {
                $this->setWarning("MSG301","会場名、分割数 ( 1以上 ) 、人数 ( 1以上 ) は必ず入力して下さい。");
                return false;
            }
            if (strlen($this->field["EXAMHALL_NAME"]) > 30) {
                $this->setWarning("MSG914","( 会場名 )");
                return false;
            }
//            $detail_capa_cnt_sum = 0;
            for ($detail_no = 1; $detail_no <= (int) $this->field["DETAIL_CNT"]; $detail_no++) {
                $name = "DETAIL_CAPA_CNT" .$detail_no;
                if (!is_numeric($this->field[$name]) || $this->field[$name] == 0) {
                    $this->setWarning("MSG301","会場名、分割数 ( 1以上 ) 、人数 ( 1以上 ) は必ず入力して下さい。");
                    return false;
                }
//                $detail_capa_cnt_sum += $this->field[$name];
            }
/***
            //人数の整合性チェック
            if ($this->field["CAPA_CNT"] != $detail_capa_cnt_sum) {
                $this->setWarning("MSG901","【会場の人数】と【分割した人数の合計】は、等しくなるように入力して下さい。");
                return false;
            }
***/
            //受付データと会場データの人数チェック
            $cnt = knjl030yQuery::receptnoCheck($this);
            $capa_cnt_hall = $cnt + $this->field["CAPA_CNT"];
            if ($this->capa_cnt_recept < $capa_cnt_hall) {
                $msgCnt = $capa_cnt_hall - $this->capa_cnt_recept;
                $this->setWarning("MSG915","座席番号生成済みリストの人数を【".$msgCnt."人】超えています。");
                return false;
            }
        }
        return true;
    }
    
    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $msg = $this->errorMessage("MSG201");
            echo <<<EOP
                <script language="javascript">                
                    top.main_frame.closeit();
                    alert('$msg');
                    top.main_frame.document.forms[0].cmd.value = "main";
                    top.main_frame.document.forms[0].submit();
                </script>
            
EOP;
        }
    }

    function &update()
    {
        $result = knjl030yQuery::update($this);
        return $result;
    }
    // 編集完了用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $msg = $this->errorMessage("MSG201");
            echo <<<EOP
                <script language="javascript">                
                    top.main_frame.closeit();
                    alert('$msg');
                    top.main_frame.document.forms[0].cmd.value = "main";
                    top.main_frame.document.forms[0].submit();
                </script>
            
EOP;
        }
    }

    function &insert()
    {
        $result = knjl030yQuery::insert($this);
        return $result;
    }
    // 削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->setMessage("MSG201");
        }
    }
    function &delete()
    {
        $result = knjl030yQuery::delete($this);
        return $result;
    }
}
?>
