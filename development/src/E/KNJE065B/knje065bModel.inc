<?php

require_once('for_php7.php');
    //定数定義
//配列内の数値のみ取得
function num_filter($var)
{
    return (is_numeric($var));
}

class knje065bModel extends Model
{
    public $cmd;
    public $gc_select;                     //セレクトメニュー
    public $control_data = array();        //コントロールのデータを保持
    public $staffcd;                       //セキュリティーチェック
    public $sec_competence;                //権限保持
    public $updated = array();             //UPDATED値保持
    public $fields = array();
    public $base_date; // 異動基準日
    public $electdiv; // 選択科目を除く
    public $hyoteiYomikae; // 評定1の場合は2で処理する
    //メンバ変数の初期化
    public function knje065bModel()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->gc_select    = VARS::post("gc_select");        //プルダウンメニュー
        $this->base_date    = VARS::post("base_date");
        if (!strlen($this->base_date)) {
            $this->base_date = str_replace("-", "/", CTRL_DATE);
        }
        $this->electdiv    = VARS::post("electdiv");
        $this->hyoteiYomikae    = VARS::post("hyoteiYomikae");

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJE065B");

        $temp = array("A_MEMBER"        => VARS::post("A_MEMBER"),
                      "B_MEMBER"        => VARS::post("B_MEMBER"),
                      "C_MEMBER"        => VARS::post("C_MEMBER"),
                      "D_MEMBER"        => VARS::post("D_MEMBER"),
                      "E_MEMBER"        => VARS::post("E_MEMBER"),
                      "COURSE_MEMBER"   => VARS::post("COURSE_MEMBER"),
                      "GRADE_MEMBER"    => VARS::post("GRADE_MEMBER"),
                      "KINDCD"          => VARS::post("KINDCD")
                      );
        $this->fields = array_merge($this->fields, $temp);

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        //データの書込み処理
        knje065bQuery::getDeleteQuery($this);
        knje065bQuery::getInsertQuery($this);
        if ($this->Properties["KNJE065B_SetCommentExACD"] == "1") {
            knje065bQuery::setHExamEntRemarkHDatQuery($this);
        }
        if ($this->Properties["knje065b_showTajuHeikin"] == "1") {
            knje065bQuery::updateAvgRank($this);
        }
        return;
    }

    //変数をクリアします
    public function clean()
    {
    }
}
