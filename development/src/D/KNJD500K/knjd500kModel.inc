<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd500kModel.inc 56580 2017-10-22 12:35:29Z maeshiro $
class knjd500kModel extends Model
{
    var $cmd;
    var $sem_cmb;           //学期
    var $gk_cmb;            //学年
    var $div_cmb;           //区分
    var $cntl_dt_year;      //処理年度(CTRL_YEAR)
    var $txt;               //入力テキスト名
    var $show_all;          //補点処理済表示フラグ
    var $score;             //成績データ
    var $sem_div;           //学期-区分
    function knjd500kModel()
    {
        ;;
    }

    function init()
    {
        $this->cntl_dt_year  = CTRL_YEAR;
        $this->cmd           = VARS::request("cmd");
        $this->sem_cmb       = VARS::post("sem_cmb");
        $this->gk_cmb        = VARS::post("gk_cmb");
        $this->div_cmb       = VARS::post("div_cmb");
        $this->show_all      = VARS::post("shw_flg");

        //セキュリティーチェック
        $this->staffcd = STAFFCD ;
        $this->sec_competence = AUTHORITY;
        
        if ($this->cmd == "update") {
            //入力データをセット
            foreach ($this->txt as $k => $name)
            {
                list($schregno,$subclasscd) = explode("-",$name);
                $this->score[$schregno][$subclasscd] = VARS::post("tbox".$k);
            }
        }
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        //得点入力画面編集対象データの書込み処理
        $result = knjd500kQuery::getUpdateQuery($this);
        return $result;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
