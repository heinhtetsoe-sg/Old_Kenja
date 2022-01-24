<?php

require_once('for_php7.php');

//定数定義
class knjxclub_detailModel extends Model {

    var $field = array();
    var $cmd;
    var $clubcd = "";
    var $date = "";
    var $seq = "";
    var $programid = "";
    var $schregno = "";
    var $g_clubcd = "";
    var $filesel = "";
    var $schreg_schkind = "";

    //メンバ変数の初期化
    function knjxclub_detailModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->programid);
            unset($this->schregno);
            unset($this->schreg_schkind);
            unset($this->g_clubcd);
            unset($this->clubcd);
            unset($this->date);
            unset($this->seq);
            unset($this->groupcd);
        }

        $this->field = array("CLUBCD" 		    =>  VARS::post("CLUBCD"),
                             "DETAIL_DATE"      =>  VARS::post("DETAIL_DATE"),
                             "MEET_NAME"        =>  VARS::post("MEET_NAME"),
                             "DIV"              =>  VARS::post("DIV"),
                             "HOSTCD"           =>  VARS::post("HOSTCD"),
                             "ITEMCD"           =>  VARS::post("ITEMCD"),
                             "KINDCD"           =>  VARS::post("KINDCD"),
                             "RECORDCD"         =>  VARS::post("RECORDCD"),
                             "DOCUMENT"         =>  VARS::post("DOCUMENT"),
                             "DETAIL_REMARK"    =>  VARS::post("DETAIL_REMARK"),
                             "DETAIL_SEQ"       =>  VARS::post("DETAIL_SEQ"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );


        if (VARS::get("CLUBCD")){
            $this->clubcd = VARS::get("CLUBCD");
        }
        if (VARS::get("DETAIL_DATE")){
            $this->date = VARS::get("DETAIL_DATE");
        }
        if (VARS::get("DETAIL_SEQ")){
            $this->seq = VARS::get("DETAIL_SEQ");
        }
        if (VARS::get("DIV")) {
            unset($this->groupcd);
            if (VARS::get("DIV") == '2' && VARS::get("GROUPCD")) {
                $this->groupcd = VARS::get("GROUPCD");
            }
        }

        if (VARS::get("PROGRAMID")){
            $this->programid = VARS::get("PROGRAMID");
        }
        if (VARS::get("SCHREGNO")){
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("G_CLUBCD")){
            $this->g_clubcd = VARS::get("G_CLUBCD");
        }
        if (VARS::get("SEND_schKind")){
            $this->schKind = VARS::get("SEND_schKind");
        }
        $this->filesel = VARS::file("FILESEL");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput()
    {
        $db = Query::dbCheckOut();

        //必須項目チェック
        if(!$this->schregno){
            $this->setWarning("生徒を指定してください。");
            return false;
        }

        if ($this->cmd == "update" || $this->cmd == "delete"){
            //リストより選択していない
            if (!$this->clubcd || !$this->date || !$this->seq){
                $this->setWarning("MSG308");
                return false;
            }
            //キー変更
            if(($this->clubcd != $this->field["CLUBCD"]) || 
               ($this->date != $this->field["DETAIL_DATE"]) || 
               ($this->seq != $this->field["DETAIL_SEQ"])){
               $this->setWarning("MSG308");
               return false;
            }
        }

        if ($this->cmd == "add"){
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
            //存在チェック
            $query = knjxclub_detailQuery::checkClubExist($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$row){
                $this->setWarning("MSG916","　　（日付）");
                return false;
            }
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

        Query::dbCheckIn($db);
        return true;
    }

    //編集完了用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    function &delete()
    {
        knjxclub_detailQuery::getDeleteQuery($this);

        if (!isset($this->warning)){
            $fchkstr = DOCUMENTROOT."/pdf_download/".SCHOOLCD.SCHOOLKIND.$this->schregno.$this->field["CLUBCD"].str_replace("/","-",$this->field["DETAIL_DATE"]).$this->seq.".pdf";
            if (file_exists($fchkstr)) {
                $this->deletePdfModel();
            }
            $this->setMessage("MSG201");
            $this->clubcd = $this->date = $this->seq = array();
            $this->field = array();
        }
        return;
    }

    //INSERT・UPDATE時の内部メソッド
    function &insert()
    {
        $seq = knjxclub_detailQuery::getInsertQuery($this);

        if (!isset($this->warning)){
            $this->clubcd = $this->field["CLUBCD"];
            $this->date = $this->field["DETAIL_DATE"];
            $this->seq = $seq;

            //PDFファイル取込
            if (!is_null($this->filesel['tmp_name']) && $this->filesel['tmp_name'] !== "") {
                $filename = DOCUMENTROOT."/pdf_download/".SCHOOLCD.SCHOOLKIND.$this->schregno.$this->field["CLUBCD"].str_replace("/","-",$this->field["DETAIL_DATE"]).$this->seq.".pdf";

                if (!is_uploaded_file($this->filesel['tmp_name'])) {
                    $this->warning = "ファイル名又は\\nサイズ(500KB以上)等が不正です。" . $this->filesel['tmp_name']." xe ".$_FILES['FILESEL']['tmp_name'];
                } else {
                    if (copy($this->filesel['tmp_name'], $filename) == FALSE){
                        $this->warning = "PDFファイルの取込失敗です。";
                    } else {
                        $this->message = "PDFファイルの取込正常です。";
                    }
                }
            }

            $this->setMessage("MSG201");
        }
        return;
    }

    //PDFファイルの削除
    function &deletePdfModel()
    {
        $retflg = true;
        if ($this->field["CLUBCD"] !== "" && $this->field["DETAIL_DATE"] !== "" && $this->seq !== "") {
            $fchkstr = DOCUMENTROOT."/pdf_download/".SCHOOLCD.SCHOOLKIND.$this->schregno.$this->field["CLUBCD"].str_replace("/","-",$this->field["DETAIL_DATE"]).$this->seq.".pdf";
            if (file_exists($fchkstr)) {
                if (!unlink($fchkstr)) {
                    $this->setMessage("MSG919","指定のPDFデータは削除できませんでした。");
                    $retflg = false;
                } else {
                    $this->message = "PDFファイルを削除しました。";
                }
            } else {
                $this->setMessage("MSG303","指定のPDFデータは存在しません。");
                $retflg = false;
            }
        } else {
            $this->setMessage("MSG303","指定のデータは存在しません。");
            $retflg = false;
        }
        return $retflg;
    }

    //PDFファイルのダウンロード
    function &getPdfModel()
    {
        if ($this->field["CLUBCD"] !== "" && $this->field["DETAIL_DATE"] !== "" && $this->seq !== "") {
            $path_file = SCHOOLCD.SCHOOLKIND.$this->schregno.$this->field["CLUBCD"].str_replace("/","-",$this->field["DETAIL_DATE"]).$this->seq.".pdf";
            $path_file = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
            $kakuTyousi = explode(".", $this->field["SENKOU_NO"]);

            $path_file = DOCUMENTROOT ."/pdf_download/".$path_file;
            /* ファイルの存在確認 */
            if (!file_exists($path_file)) {
                $this->setWarning("MSG303","指定のPDFデータは存在しません。".$path_file);
                return false;
            }

            /* オープンできるか確認 */
            if (!($fp = fopen($path_file, "r"))) {
                $this->setWarning("MSG203","指定のPDFデータは開くことができません。");
                return false;
            }
            fclose($fp);

            /* ファイルサイズの確認 */
            if (($content_length = filesize($path_file)) == 0) {
                $this->setWarning("MSG203","指定のPDFデータのファイルサイズは 0 byteです。");
                return false;
            }

            ini_set('mbstring.http_output', 'pass');
            /* ダウンロード用のHTTPヘッダ送信 */
            header('Content-Type: application/pdf');
            common::setDownloadHeader($path_file);
            header("Content-Length: ".$content_length);

            /* ファイルを読んで出力 */
            if (!readfile($path_file)) {
                $this->setWarning("MSG203","指定のPDFデータは開くことができません。");
                return false;
            }
        } else {
            $this->setWarning("MSG303","指定のPDFデータは存在しません。");
            return false;
        }

        return true;
    }

}
?>
