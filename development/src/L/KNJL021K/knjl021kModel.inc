<?php

require_once('for_php7.php');

//定数定義

class knjl021kModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;
    var $selectdata3;
    var $center_title;

    //メンバ変数の初期化
    function knjl021kModel()
    {
    }

    //init
    function init()
    {
        $this->cmd             = VARS::request("cmd");
        $this->ObjYear         = CTRL_YEAR + 1;
        $this->selectdata      = preg_split("/,/",VARS::post("selectdata"));    //志願者データで上書き
        $this->selectdata2     = preg_split("/,/",VARS::post("selectdata2"));   //変更なし
        $this->selectdata3     = preg_split("/,/",VARS::post("selectdata3"));   //事前相談データで上書き
        $this->testdiv         = VARS::post("TESTDIV");
        $this->center_title    = VARS::post("CENTER_TITLE");
        $this->err_selectdata  = array();
        $this->err_selectdata3 = array();
        $this->updateInfo      = array("OK" => 0, "PS_ERR" => 0, "FS_ERR" => 0);    //エラーカウンタ
    }

    //データチェック
    function validateInput()
    {
        return true;
    }

    //更新メイン
    function &getUpdateModel()
    {
        $this->err_selectdata  = array();
        $this->err_selectdata3 = array();
        $this->updateInfo      = array("OK" => 0, "PS_ERR" => 0, "FS_ERR" => 0);    //エラーカウンタ
        //データチェック
        if ($this->validateInput()) {
            //データ上段(塾データ)
            $this->updateExe($this->selectdata, "PS", 2);
            //データ下段(学校データ)
            $this->updateExe($this->selectdata3, "FS", 1);
        }
        $this->updateInfo["PS_ERR"] = (get_count($this->err_selectdata) > 0 && $this->err_selectdata[0] != "") ?get_count($this->err_selectdata) : 0 ;
        $this->updateInfo["FS_ERR"] = (get_count($this->err_selectdata3) > 0 && $this->err_selectdata3[0] != "") ?get_count($this->err_selectdata3) : 0 ;
        $msOk = "\\n正常終了  ：".$this->updateInfo["OK"]."件\\n";
        $msPs = "エラー塾　　：".$this->updateInfo["PS_ERR"]."件\\n";
        $msFs = "エラー学校 ：".$this->updateInfo["FS_ERR"]."件";
        $this->setMessage("MSG202",$msOk.$msPs.$msFs);

    }

    //更新処理
    function updateExe($selectData, $field, $datadiv)
    {
        for ($i = 0; $i < get_count($selectData); $i++) {
            $base = preg_split("/-/",$selectData[$i]);
            if ($base[3] == 2) {
                $basecnt = 2;
                $copycnt = 1;
            } else {
                $basecnt = 1;
                $copycnt = 2;
            }
            $baseaccept = $base[$basecnt];
            $copyaccept = $base[$copycnt];
            $checkJudge = knjl021kQuery::getUpdateCheckQuery($this, $copyaccept, $field, $datadiv);
            if ("" == $checkJudge || 0 >= $checkJudge) {
                if ($field == "PS") {
                    $this->err_selectdata[]  = $baseaccept;
                    $this->errdata  = $baseaccept;
                } else {
                    $this->err_selectdata3[] = $baseaccept;
                }
                continue;
            }
            $this->updateInfo["OK"]++;
            $this->update($baseaccept, $copyaccept, $field, $datadiv);
        }
    }

    //更新実行
    function &update($baseaccept, $copyaccept, $field, $datadiv)
    {
        knjl021kQuery::getHdatUpdateQuery($this, $baseaccept, $copyaccept, $field);
        knjl021kQuery::getDatDeleteQuery($this, $baseaccept, $datadiv);
        knjl021kQuery::getDatInsertQuery($this, $baseaccept, $copyaccept, $datadiv);

        return $result;
    }

}
?>
