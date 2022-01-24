<?php

require_once('for_php7.php');

//定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knje065kModel extends Model {
    var $cmd;
    var $gc_select;                     //セレクトメニュー
    var $control_data = array();        //コントロールのデータを保持
    var $staffcd;                       //セキュリティーチェック
    var $sec_competence;                //権限保持
    var $updated = array();             //UPDATED値保持
    var $fields = array();
    var $radio = 1;
    var $base_date;                     //在籍基準日 2005.10.17 minei

    //メンバ変数の初期化
    function knje065kModel()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
        $this->radio = 1;
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->gc_select    = VARS::post("gc_select");  //プルダウンメニュー
        $this->base_date    = VARS::post("base_date");  //在籍基準日 2005.10.17 minei
        //2005/10/19 arakaki
        if (!strlen($this->base_date)) {
            $this->base_date = str_replace("-", "/", CTRL_DATE);
        }

        if (VARS::post("RADIO")){
            $this->radio        = VARS::post("RADIO");
        }
        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd,"KNJE065K");

        $temp = array("A_MEMBER"        => VARS::post("A_MEMBER"),
                      "B_MEMBER"        => VARS::post("B_MEMBER"),
                      "C_MEMBER"        => VARS::post("C_MEMBER"),
                      "D_MEMBER"        => VARS::post("D_MEMBER"),
                      "E_MEMBER"        => VARS::post("E_MEMBER"),
                      "COURSE_MEMBER"   => VARS::post("COURSE_MEMBER"),
                      "GRADE_MEMBER"    => VARS::post("GRADE_MEMBER")
                      );
        $this->fields = array_merge($this->fields,$temp);
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &update()
    {
        //データの書込み処理
        knje065kQuery::getDeleteQuery($this);
        knje065kQuery::getInsertQuery($this);
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
