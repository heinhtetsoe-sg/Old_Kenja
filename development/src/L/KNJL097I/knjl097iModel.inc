<?php

class knjl097iModel extends model
{
    var $field = array();
    var $cmd;
    var $hid_receptno;
    var $ObjYear;
    var $score;
    var $data;

    function knjl097iModel()
    {
    }

    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");
        

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "EXAM_TYPE"        =>  "1",                            //受験型
                             "SORT"             =>  VARS::post("SORT"),             //表示順
                             "SEX"              =>  VARS::post("SEX"),              //抽出
                             "JUDGEDIV"        =>  VARS::post("JUDGEDIV")         //合否
                             );

        $this->hid_receptno = VARS::post("HID_RECEPTNO");
        $this->checkAll     = VARS::post("CHECKALL");

        //画面上の検索条件が変わったら、選択状態を保持する意味が無いので、それ以外でのみ選択状態を保持(※取消も対象)
        $this->data = array();
        if ($this->cmd != "main") {
            $array = explode(",", $this->hid_receptno);
            foreach ($array as $key => $receptno) {
                $this->data["CHECKED"][$receptno] = VARS::post("CHECKED_".$receptno);
            }
        }
    }

    function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->score = array();
        //備考・評価を再配置
        $array = explode(",", $this->hid_receptno);
        foreach ($array as $key => $receptno) {
            //チェックが付いてる箇所の値を取得する。
            if (VARS::post("CHECKED_".$receptno) == "1") {
                $this->score[$receptno]["JUDGEDIV"] = VARS::post("JUDGEDIV");
            }
        }

        //更新処理
        $result = knjl097iQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201", $prtStr);
        }
    }
}
?>
