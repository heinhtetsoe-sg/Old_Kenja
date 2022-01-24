<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj120Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjj120Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {               
        $this->field = array("SCHKIND"          =>  VARS::post("SCHKIND"),          //校種
                             "CLUBCD"           =>  VARS::post("CLUBCD"),           //部クラブ
                             "SCHREGNO"         =>  VARS::post("selectdata"),       //対象生徒
                             "DETAIL_DATE"      =>  VARS::post("DETAIL_DATE"),      //日付
                             "MEET_NAME"        =>  VARS::post("MEET_NAME"),        //大会名称
                             "DIV"              =>  VARS::post("DIV"),              //区分
                             "HOSTCD"           =>  VARS::post("HOSTCD"),           //開催地域
                             "ITEMCD"           =>  VARS::post("ITEMCD"),           //種目
                             "KINDCD"           =>  VARS::post("KINDCD"),           //種目種類
                             "RECORDCD"         =>  VARS::post("RECORDCD"),         //成績
                             "DOCUMENT"         =>  VARS::post("DOCUMENT"),         //記録
                             "DETAIL_REMARK"    =>  VARS::post("DETAIL_REMARK")     //備考
                            );
        $this->filesel = VARS::file("FILESEL");
        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //必須項目チェック
        if(!$this->field["CLUBCD"]){
            $this->setWarning("MSG301","　　（部クラブ）");
            return false;
        }
        if(!$this->field["DETAIL_DATE"]){
            $this->setWarning("MSG301","　　（日付）");
            return false;
        }

        //日付チェック
        $sdate = CTRL_YEAR.'/04/01';
        $edate = (CTRL_YEAR+1).'/03/31';
        if($this->field["DETAIL_DATE"] < $sdate || $edate < $this->field["DETAIL_DATE"]){
            $this->setWarning("MSG901","(".$sdate."～".$edate.")");
            return false;
        }

        //桁数チェック
        if (strlen($this->field["MEET_NAME"]) > 90 ) {
            $this->setWarning("MSG914","(大会名称は90バイトまでです)");
            return false;  
        }
        if (strlen($this->field["DOCUMENT"]) > 60 ) {
            $this->setWarning("MSG914","(記録は60バイトまでです)");
            return false;  
        }
        if (strlen($this->field["DETAIL_REMARK"]) > 60 ) {
            $this->setWarning("MSG914","(備考は60バイトまでです)");
            return false;  
        }

        return true;
    }

    //更新
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            //アップロードファイル名称の格納変数を初期化
            $upfilename = array();

            //選択生徒のデータを更新
            $cnt = knjj120Query::UpdateQuery($this, $upfilename);
            
            if ($cnt > 0 && $target !== "") {
                //ファイルのアップロード(選択生徒数分作成)

                $this->getExecModel($upfilename);
            }

            $total = get_count(explode(',', $this->field["SCHREGNO"]));
            $this->setMessage("MSG201","　　（".$cnt."件／".$total."件）");

            $schkind     = $this->field["SCHKIND"];
            $clubcd      = $this->field["CLUBCD"];
            $detail_date = $this->field["DETAIL_DATE"];
            $meet_name   = $this->field["MEET_NAME"];
            $this->field = "";
            $this->field["SCHKIND"]     = $schkind;
            $this->field["CLUBCD"]      = $clubcd;
            $this->field["DETAIL_DATE"] = $detail_date;
            $this->field["MEET_NAME"]   = $meet_name;
        }
        return;
    }

    //アップロード
    function &getExecModel($upfilename)
    {
        //ファイル名取得
        $target = $_FILES['FILESEL']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        //$kakutyousi = strtoupper($kakutyousi);

        foreach ($upfilename as $val) {
            $filename = DOCUMENTROOT ."/" .'pdf_download/' . $val .$kakutyousi;

            if (!is_uploaded_file($this->filesel['tmp_name'])) {
                $this->warning = "ファイル名又は\\nサイズ(500KB以上)等が不正です。";
            } else {
                if (copy($this->filesel['tmp_name'], $filename) == FALSE){
                    $this->warning = "PDFファイルの取込失敗です。";
                } else {
                    $this->message = "PDFファイルの取込正常です。";
                }
            }
        }
    }
}
?>
