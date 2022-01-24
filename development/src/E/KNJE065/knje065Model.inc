<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knje065Model extends Model {
    var $cmd;
    var $gc_select;                     //セレクトメニュー
    var $control_data = array();        //コントロールのデータを保持
    var $staffcd;                       //セキュリティーチェック
    var $sec_competence;                //権限保持
    var $updated = array();             //UPDATED値保持
    var $fields = array();
    var $hyoteiYomikae; // 評定1の場合は2で処理する
    //メンバ変数の初期化
    function knje065Model()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->gc_select    = VARS::post("gc_select");        //プルダウンメニュー
        $this->hyoteiYomikae    = VARS::post("hyoteiYomikae");

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd,"KNJE065");

        $temp = array("A_MEMBER"        => VARS::post("A_MEMBER"),
                      "B_MEMBER"        => VARS::post("B_MEMBER"),
                      "C_MEMBER"        => VARS::post("C_MEMBER"),
                      "D_MEMBER"        => VARS::post("D_MEMBER"),
                      "E_MEMBER"        => VARS::post("E_MEMBER"),
                      "COURSE_MEMBER"   => VARS::post("COURSE_MEMBER"),
                      "GRADE_MEMBER"    => VARS::post("GRADE_MEMBER")
                      );
        $this->fields = array_merge($this->fields,$temp);

        $this->getPropertiesAll(); //プロパティファイルの読込み
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
        knje065Query::getDeleteQuery($this);
        knje065Query::getInsertQuery($this);
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
