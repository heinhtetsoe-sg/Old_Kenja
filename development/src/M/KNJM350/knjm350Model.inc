<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjm350Model.inc 56590 2017-10-22 13:01:54Z maeshiro $
//メニュー画面
class knjm350Model extends model
{
    var $cmd;                           //一覧表示時のcmd
    var $ObjYear;                       //年度
    var $sub;                           //講座コード+科目コード
    var $sch;                           //更新用学籍番号
    var $stsq;                          //更新用回数
    var $rsq;                           //更新用回数
    var $rdat;                          //更新用日付
    var $allcheck;                      //全選択チェックボックス
    var $repcntall;                     //回数
    var $setdata = array();             //表示データ配列更新用
    var $kaisu_i = array();             //2006/07/28伊集追加
    var $kaisu = array();               //回数
    var $error_flg = false;             //入力エラーフラグ
    var $Date;                          //日付
    
    function knjm350Model()
    {
        $this->ObjYear = CTRL_YEAR;
    }
        
    function init()
    {
        $this->cmd    = VARS::request("cmd");

        $this->ObjYear    = VARS::request("YEAR");

        $this->Date  = VARS::request("DATE");

        $this->sub = VARS::post("SELSUB");
        if ($this->cmd == 'allcheck'){
            $this->allcheck = VARS::post("ALLCHECK");
        }else {
            $this->allcheck = "off";
        }

        if ($this->cmd == 'update'){

            $icount = 0;        //2006/07/28伊集追加

            for ($i = 0; $i < $this->repcntall ; $i++){
                $this->kaisu["DELCHK"][$i] = VARS::post("DELCHK".$i);                   //2006/07/28伊集追加

                if (substr(VARS::post("DELCHK".$i),0,2) == "on"){                       //2006/07/28伊集追加
                    $this->kaisu_i["DELCHK"][$icount] = VARS::post("DELCHK".$i);        //2006/07/28伊集追加
                    $icount++;                                                          //2006/07/28伊集追加
                }                                                                       //2006/07/28伊集追加

            }

        }
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {
        $this->setdata = array();               //表示データ配列
    }

    //入力項目チェック
    function validateInput()
    {
        $this->error_flg = false;
        $checkflg = 0;
        $checdata = array();
        $setcnt   = 0;

        for ($i = 0; $i < $this->repcntall ; $i++){

            $checdata["DELCHK"][$i] = VARS::post("DELCHK".$i);
            if (substr($checdata["DELCHK"][$i],0,2) == "on"){
                $this->setdata["DELCHK"][$setcnt] = VARS::post("DELCHK".$i);
                $checkflg = 1;
                $setcnt++;
            }
        }
        if ($checkflg == 0) $this->setWarning("印刷項目が指定されていません。");
        return true;
    }
    
    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $tmpdata = array();
        //入力チェック
        if ($this->validateInput()){
            for ($upcnt = 0;$upcnt < get_count($this->setdata["DELCHK"]);$upcnt++){
                $tmpdata  = explode(":",$this->setdata["DELCHK"][$upcnt]);
                $this->sch  = $tmpdata[1];
                $this->stsq = $tmpdata[2];
                $this->rsq  = $tmpdata[3];
                $this->rdat = $tmpdata[4];
                $this->update();
            }
        }
    }

    function &update()
    {
        $result = knjm350Query::getUpdateQuery($this);

        return $result;
    }

}
?>
