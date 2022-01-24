<?php

require_once('for_php7.php');

//メニュー画面
class knjl010hModel extends model
{
    var $cmd;               //一覧表示時のcmd
    var $examno;
    var $applicantdiv;
    var $field;
    var $ObjYear;           //入試年度
    var $error_flg = false; //入力エラーフラグ
    
    function knjl010hModel()
    {
    }

    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->cmd    = VARS::request("cmd");
        
        if ($this->cmd == "read" || $this->cmd == "next" || $this->cmd == "back") {
            $this->examno = sprintf("%04d",VARS::post("EXAMNO"));
        }

        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        
        $this->field = array("RECOM_EXAMNO" => VARS::post("RECOM_EXAMNO"),
                             "SEX"       => VARS::post("SEX"),
                             "FS_CD"     => VARS::post("FS_CD"),
                             "FS_NATPUBPRIDIV"   => VARS::post("FS_NATPUBPRIDIV"));
        for ($i = 0; $i < 50; $i++) {
            $this->field["TESTDIV1"."-".$i][$i] = VARS::post("TESTDIV1"."-".$i);
            $this->field["TESTDIV2"."-".$i][$i] = VARS::post("TESTDIV2"."-".$i);
            $this->field["TESTDIV3"."-".$i][$i] = VARS::post("TESTDIV3"."-".$i);

            $this->field["NAME"][$i]        = VARS::post("NAME" .$i);
            $this->field["NAME_KANA"][$i]   = VARS::post("NAME_KANA" .$i);
        }
    }

    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {
        if ($this->cmd == "back") {
            $this->examno = sprintf("%04d",((int)$this->examno - 50));
            if ((int)$this->examno < 1) {
                $this->examno = "0001";
            } elseif ((int)$this->examno > 9999) {
                $this->examno = "9999";
            }
        }

        if ($this->cmd == "next") {
            $this->examno = sprintf("%04d",((int)$this->e_examno + 1));
            if ((int)$this->examno < 1) {
                $this->examno = "0001";
            } elseif ((int)$this->examno > 9999) {
                $this->examno = "9999";
            }
        }
        //更新処理時には終了受験番号を変更しない
        if ($this->cmd != "read2") {
            $this->e_examno = sprintf("%04d", (int)$this->examno + 49);
            if ((int)$this->e_examno > 9999) $this->e_examno = "9999";   //最大9999までにする
            $this->error_flg = false;
        }
        if ($this->cmd == "reset")
            $this->cmd = "read";
    }

    //入力項目チェック
    function validateInput()
    {
        $this->error_flg = false;

        //入力値（推薦受験番号）の重複チェック
        $temp = array_count_values($this->field["RECOM_EXAMNO"]);
        foreach ($temp as $key => $val)
        {
            if (strlen($key) && $val > 1) {
                $this->setWarning("MSG302", "推薦受験番号：".$key);
                $this->error_flg = true;
                return false;
            }
        }
        for ($i = 0; $i < 50; $i++)
        {
            $examno = sprintf("%04d", (int)$this->examno + $i);

            $this->field["NAME"][$i] = ltrim($this->field["NAME"][$i]);
            $this->field["NAME"][$i] = rtrim($this->field["NAME"][$i]);
            if (strlen($this->field["NAME"][$i]) > 60) {
                $this->setWarning("MSG914", "受験番号 ".$examno."（氏名60バイトまで）");
                $this->error_flg = true;
                return false;
            }
            $this->field["NAME_KANA"][$i] = ltrim($this->field["NAME_KANA"][$i]);
            $this->field["NAME_KANA"][$i] = rtrim($this->field["NAME_KANA"][$i]);
            if (strlen($this->field["NAME_KANA"][$i]) > 120) {
                $this->setWarning("MSG914", "受験番号 ".$examno."（氏名かな120バイトまで）");
                $this->error_flg = true;
                return false;
            }
            //推薦受験番号チェック
            if ($this->applicantdiv != "2" && strlen($this->field["RECOM_EXAMNO"][$i])) {
                $this->setWarning("MSG901", "受験番号 ".$examno."（推薦受験番号）\\n入力可能なのは、高校一般入試のみです。");
                $this->error_flg = true;
                return false;
            }
            if (strlen($this->field["RECOM_EXAMNO"][$i])) {
                //存在チェック
                $retCnt = knjl010hQuery::checkRecomExamno($this, $this->field["RECOM_EXAMNO"][$i]);
                if (0 < $retCnt) {
                    //重複チェック
                    $retCnt2 = knjl010hQuery::checkRecomExamno2($this, $this->field["RECOM_EXAMNO"][$i], $examno);
                    if (0 < $retCnt2) {
                        $this->setWarning("MSG901", "受験番号 ".$examno."（推薦受験番号）\\n入力された推薦受験番号は既に登録されています。");
                        $this->error_flg = true;
                        return false;
                    }
                } else {
                    $this->setWarning("MSG901", "受験番号 ".$examno."（推薦受験番号）\\n入力された推薦受験番号のデータは存在しません。");
                    $this->error_flg = true;
                    return false;
                }
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
            $this->setMessage("MSG201");
        }
    }

    function &update()
    {
        $result = knjl010hQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
