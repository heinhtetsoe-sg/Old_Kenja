<?php

require_once('for_php7.php');

//メニュー画面
class knjl010oModel extends model
{
    var $cmd;               //一覧表示時のcmd
    var $examno;
    var $applicantdiv;
    var $testdiv;
    var $field;
    var $ObjYear;           //入試年度
    var $error_flg = false; //入力エラーフラグ
    
    function knjl010oModel()
    {
        $this->ObjYear = CTRL_YEAR+1;
    }
        
    function init()
    {
        $this->cmd    = VARS::request("cmd");
        
        if ($this->cmd == "read" || $this->cmd == "next" || $this->cmd == "back") {
            $this->examno = sprintf("%05d",VARS::post("EXAMNO"));
        }

        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        
        $this->field = array("NAME"      => VARS::post("NAME"),
                             "NAME_KANA" => VARS::post("NAME_KANA"),
                             "SEX"       => VARS::post("SEX"),
                             "FS_CD"     => VARS::post("FS_CD"),
                             "FS_NAME"   => VARS::post("FS_NAME"),
                             "FS_AREA_CD"   => VARS::post("FS_AREA_CD"),
                             "TESTDIV"   => VARS::post("TESTDIV"),
                             "DESIREDIV" => VARS::post("DESIREDIV"));
        for ($i = 0; $i < 50; $i++) {
            for ($code = 0; $code <= 6; $code++) {
                $fieldname      = "TESTDIV" .$code;
                $fieldnameObj   = $fieldname ."-" .$i;
                $this->field[$fieldnameObj][$i] = VARS::post($fieldnameObj);
            }
        }
    }

    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {
        if ($this->cmd == "back") {
            $this->examno = sprintf("%05d",((int)$this->examno - 50));
            if ((int)$this->examno < 1) {
                $this->examno = "00001";
            } elseif ((int)$this->examno > 99999) {
                $this->examno = "99999";
            }
        }

        if ($this->cmd == "next") {
            $this->examno = sprintf("%05d",((int)$this->e_examno + 1));
            if ((int)$this->examno < 1) {
                $this->examno = "00001";
            } elseif ((int)$this->examno > 99999) {
                $this->examno = "99999";
            }
        }
        //更新処理時には終了受験番号を変更しない
        if ($this->cmd != "read2") {
            $this->e_examno = sprintf("%05d", (int)$this->examno + 49);
            if ((int)$this->e_examno > 99999) $this->e_examno = "99999";   //最大99999までにする
            $this->error_flg = false;
        }
        if ($this->cmd == "reset")
            $this->cmd = "read";
    }

    //入力項目チェック
    function validateInput()
    {
        $this->error_flg = false;

        for ($i = 0; $i < 50; $i++)
        {
            $this->field["NAME"][$i] = ltrim($this->field["NAME"][$i]);
            $this->field["NAME"][$i] = rtrim($this->field["NAME"][$i]);
            if (strlen($this->field["NAME"][$i]) > 60) {
                $this->setWarning("MSG914", "受験番号 ".sprintf("%05d", (int)$this->examno + $i)."（氏名60バイトまで）");
                $this->error_flg = true;
                return false;
            }
            $this->field["NAME_KANA"][$i] = ltrim($this->field["NAME_KANA"][$i]);
            $this->field["NAME_KANA"][$i] = rtrim($this->field["NAME_KANA"][$i]);
            if (strlen($this->field["NAME_KANA"][$i]) > 120) {
                $this->setWarning("MSG914", "受験番号 ".sprintf("%05d", (int)$this->examno + $i)."（氏名かな120バイトまで）");
                $this->error_flg = true;
                return false;
            }
            $this->field["FS_NAME"][$i] = ltrim($this->field["FS_NAME"][$i]);
            $this->field["FS_NAME"][$i] = rtrim($this->field["FS_NAME"][$i]);
            $cntData = knjl010oQuery::getCntData($this, (int)$this->examno + $i);
            if (strlen($this->field["FS_NAME"][$i]) > 45 || $cntData == 0 && strlen($this->field["FS_NAME"][$i]) > 36) {
                $this->setWarning("MSG914", "受験番号 ".sprintf("%05d", (int)$this->examno + $i)."（出身学校名45バイトまで）");
                $this->error_flg = true;
                return false;
            }
            //入試区分チェック
            //全ての項目がない場合は削除するのでチェック不要
            if (!strlen($this->field["NAME"][$i]) && 
                !strlen($this->field["NAME_KANA"][$i]) && 
                !strlen($this->field["SEX"][$i]) && 
                !strlen($this->field["FS_CD"][$i]) && 
                !strlen($this->field["FS_NAME"][$i]) && 
                !strlen($this->field["FS_AREA_CD"][$i]) && 
                !strlen($this->field["TESTDIV0"."-".$i][$i]) && 
                !strlen($this->field["TESTDIV1"."-".$i][$i]) && 
                !strlen($this->field["TESTDIV2"."-".$i][$i]) && 
                !strlen($this->field["TESTDIV3"."-".$i][$i]) && 
                !strlen($this->field["TESTDIV4"."-".$i][$i]) && 
                !strlen($this->field["TESTDIV5"."-".$i][$i]) && 
                !strlen($this->field["TESTDIV6"."-".$i][$i])) 
            {
            //項目が１つでもある場合は入試区分チェックする
            } else if (!strlen($this->field["TESTDIV0"."-".$i][$i]) && 
                       !strlen($this->field["TESTDIV1"."-".$i][$i]) && 
                       !strlen($this->field["TESTDIV2"."-".$i][$i]) && 
                       !strlen($this->field["TESTDIV3"."-".$i][$i]) && 
                       !strlen($this->field["TESTDIV4"."-".$i][$i]) && 
                       !strlen($this->field["TESTDIV5"."-".$i][$i]) && 
                       !strlen($this->field["TESTDIV6"."-".$i][$i])) 
            {
                $this->setWarning("MSG301", "受験番号 ".sprintf("%05d", (int)$this->examno + $i)."（入試区分）");
                $this->error_flg = true;
                return false;
            }
            //同時受験不可チェック用 同じ入試日の入試区分は同時受験不可
            $testdivArray = array();
            for ($testdiv = 0; $testdiv <= 6; $testdiv++) {
                if (strlen($this->field["TESTDIV".$testdiv."-".$i][$i])) {
                    $testdivArray[] = $testdiv;
                }
            }
            $testDayCheck = array();
            if (0 < get_count($testdivArray)) {
                $testDayCheck = knjl010oQuery::testDayCheck($this->ObjYear,$testdivArray);
            }
            if (1 < get_count($testDayCheck)) {
                $msg  = "受験番号 ".sprintf("%05d", (int)$this->examno + $i)."（入試区分）";
                $msg .= "\\n「" .implode("」と「", $testDayCheck) ."」を同時に受験できません。\\nどちらかを選択してください。";
                $this->setWarning("MSG901", $msg);
                $this->error_flg = true;
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
            $this->setMessage("MSG201");
        }
    }

    function &update()
    {
        $result = knjl010oQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
