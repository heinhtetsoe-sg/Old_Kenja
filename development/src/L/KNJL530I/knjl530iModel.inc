<?php
//メニュー画面
class knjl530iModel extends model
{
    public $cmd;                   //一覧表示時のcmd
    public $year;                  //入試年度
    public $mode;
    public $testdiv;
    public $applicantdiv;
    public $exam_type;             //学科に流用
    public $field = array();

    public function knjl530iModel()
    {
    }

    public function init()
    {
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;

        if (VARS::get("PROGRAMID")) {
            unset($this->examhallcd);
            unset($this->testdiv);
            unset($this->applicantdiv);
            unset($this->exam_type);
            unset($this->mode);
        }

        if (VARS::get("examhallcd")) {
            $this->examhallcd   = VARS::get("examhallcd");
        }
        if (VARS::post("TESTDIV")) {
            $this->testdiv   = VARS::post("TESTDIV");
        }
        $this->applicantdiv   = "2";//固定値
        //学科
        if (VARS::post("EXAM_TYPE")) {
            $this->exam_type   = VARS::post("EXAM_TYPE");
        }
        if (VARS::request("mode")) {
            $this->mode   = VARS::request("mode");
        }

        //会場名
        $this->field["EXAMHALL_NAME"] = VARS::post("EXAMHALL_NAME");
        //人数
        $this->field["CAPA_CNT"]    = VARS::post("CAPA_CNT");
        $this->field["CHECKED"]     = VARS::post("CHECKED");

        if ($this->mode == "insert") {
            unset($this->examhallcd);
        }

        //学科リスト
        $this->examTypeList = array(
            "1" => "普通科",
            "2" => "工業科",
        );
    }

    /**
     * メイン表示用のモデルを返します
     */
    public function &getMainModel()
    {
    }

    //入力項目チェック
    public function validateInput()
    {
        if (!strlen($this->testdiv)) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if ($this->cmd == "delete") {
            if (!is_array($this->field["CHECKED"])) {
                $this->warning = "チェックボックスが選択されていません。";
                return false;
            }
        } elseif ($this->cmd == "insert" || $this->cmd == "update") {
            if (strlen($this->field["EXAMHALL_NAME"]) == 0 || !is_numeric($this->field["CAPA_CNT"]) || $this->field["CAPA_CNT"] == 0) {
                $this->setWarning("MSG301", "会場名、人数 ( 1以上 ) は必ず入力して下さい。");
                return false;
            }
            if (strlen($this->field["EXAMHALL_NAME"]) > 30) {
                $this->setWarning("MSG914", "( 会場名 )");
                return false;
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
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

    public function &update()
    {
        $result = knjl530iQuery::update($this);
        return $result;
    }

    // 編集完了用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //割り振り実行
            knjl530iQuery::exec($db, $this);
            $db->commit();
            Query::dbCheckIn($db);

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

    public function &insert()
    {
        $result = knjl530iQuery::insert($this);
        return $result;
    }

    // 割り振り実行のモデルを返します
    public function &getExecModel()
    {
        if ($this->validateInput()) {
            $this->exec();
            $this->setMessage("MSG201");
        }
    }

    public function &exec()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //割り振り実行
        knjl530iQuery::exec($db, $this);
        $db->commit();
        Query::dbCheckIn($db);
    }

    // 削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
            $this->setMessage("MSG201");
        }
    }

    public function &delete()
    {
        $result = knjl530iQuery::delete($this);
        return $result;
    }
}
?>
