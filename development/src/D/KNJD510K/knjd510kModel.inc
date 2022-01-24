<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd510kModel.inc 56580 2017-10-22 12:35:29Z maeshiro $
class knjd510kModel extends Model
{
    var $cmd;               //cmd
    var $sem_cmb;           //学期
    var $gk_cmb;            //学年
    var $cntl_dt_year;      //処理年度(CTRL_YEAR)
    var $show_all;          //補点処理済表示フラグ
    var $dataCount;         //データ件数
    var $sec_competence;    //権限
    var $txt_names;         //補充入力テキストボックス名
    var $score = array();
    var $score2 = array();
    var $on_chk_box;           //チェックされたレコード
    function knjd510kModel()
    {
        ;;
    }

    function init()
    {
        $this->sec_competence = AUTHORITY;                  //権限
        $this->cntl_dt_year   = CTRL_YEAR;                  //処理年度
        $this->cmd            = VARS::request("cmd");       //cmd
        $this->sem_cmb        = VARS::post("sem_cmb");
        $this->gk_cmb         = VARS::post("gk_cmb");       //学年コンボボックス
        $this->show_all       = VARS::post("shw_flg");      //補充処理済み表示フラグ
        $this->dataCount      = VARS::post("dataCount");    //データ件数カウンター

        if($this->cmd == "update")
        {
            $this->score = array();
            $this->score2 = array();
            $this->on_chk_box = array();
            
            if (is_array(VARS::post("chk_box"))) {
                $this->on_chk_box = VARS::post("chk_box");
            }

            foreach ($this->txt_names as $k => $name)
            {
               list($col,$key) = explode("-",$name);

               list($subclasscd, $schregno) = explode(",", $key);

                //チェックされているのみ取得
               if (in_array($key, $this->on_chk_box)) 
               {
                   $this->score[$schregno][$subclasscd][$col] = VARS::post("tbox".$k);
                
               //チェックされてないレコード 
               } else {
                   $this->score2[$schregno][$subclasscd][$col] = VARS::post("tbox".$k);
               }
            }

        }
    }

    // 入力項目チェック
    function validateInput()
    {
        /* 取得データはJavaScript内にてチェック */
        return true;
    }

    // アップデート
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }else{
                $this->setWarning("MSG917");
            }
        }
    }

    // アップデート時の内部メソッド
    function &update()
    {
        //得点入力画面編集対象データの書込み処理
        return knjd510kQuery::getUpdateQuery($this);
    }

    // 変数をクリアします
    function clean()
    {
        ;;
    }
}
?>
