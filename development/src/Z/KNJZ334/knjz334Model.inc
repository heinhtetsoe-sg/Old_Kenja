<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjz334Model extends Model {
    var $field      = array();
    var $cmd;
    var $subclasscd;        //primary
    var $year_code;
    var $isload;
    var $tmp;				//NO001
    var $year;				//NO001
    
    /**
     * メンバ変数の初期化
     */
    function knjz334Model()
    {
        unset($this->seldata);
        $this->allDelFlg = false;
    }
    function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->dataDiv = '01';

        $this->getPropertiesAll(); //プロパティファイル

        
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataLabel  = VARS::post("selectdataLabel");

        $this->field = array("ANNOUNCE"         =>  VARS::post("NAIYO"),                 //右側入力内容
                             "ANNOUNCE_ENG"     =>  VARS::post("NAIYO_ENG"),             //右側入力内容英語
                             "SELECT_GROUP"     =>  VARS::post("GROUP"),                 //右側選んだグループ
                             "FROM"             =>  VARS::post("FROM"),                  //右側選択した開始日
                             "TO"               =>  VARS::post("TO"),                    //右側選択した最終日
                             "SELECT_INDNAME"   =>  VARS::post("SELECT_INDNAME")         //右側選択した個人
                             );
                             
        $this->seldata = VARS::request("OSHIRASE_NO") != "" ? VARS::request("OSHIRASE_NO") : $this->seldata;

        if($this->cmd == "edit" || $this->cmd == "comp"){
            $this->seldata = "";
            $this->selmode = "";
            $this->allDelFlg = false;
        }

        if ($this->cmd == "choice") {
            $this->allDelFlg = false;
        }

        if($this->cmd == "indiv" || $this->cmd == "change_grp"){
            $this->selmode = "indiv";
        }else if($this->cmd == "update" || $this->cmd == "add" || $this->cmd == "indupdate" || $this->cmd == "indadd"){
            $this->selmode = $this->selmode;
        }else{
            $this->selmode = "";
        }
        $this->file = VARS::file("FILE");

    }
    
    /*挿入*/
    function getInsertModel()
    {
        unset($this->update);
        unset($this->grpup);
        unset($this->indup);
        
        //GROUPが選択されてるかのエラーチェック
        if($this->selmode == ""){
            if(!is_array($this->field["SELECT_GROUP"])){
                $this->setMessage("対象のグループを選択してください");
                $this->error = "1";
                return false;
            }else{
                $this->error = "";
            }
        }else{
            if($this->selectdata == ""){
                $this->setMessage("対象の個人を選択してください");
                $this->error = "1";
                return false;
            }else{
                $this->error = "";
            }
        }
        $db = Query::dbCheckOut();
        
        //OSHIRASE_TBL挿入
        //OSHIRASE_TBLのOSHIRASE_NOの最大値取得
        $maxQuery = knjz334Query::getMax($this, "TBL");
        $tblMax = $db->getOne($maxQuery);
        $tblMax = $tblMax + 1;
        
        $this->update["DATA_DIV"] = $this->dataDiv;
        $this->update["OSHIRASE_NO"] = $tblMax;
        $this->update["ENTRY_DATE"] = date("Y-m-d");
        $this->update["ANNOUNCE"] = $this->field["ANNOUNCE"];
        $this->update["ANNOUNCE_ENG"] = $this->field["ANNOUNCE_ENG"];
        $this->update["START_DATE"] = str_replace("/", "-", $this->field["FROM"]);
        $this->update["END_DATE"] = str_replace("/", "-", $this->field["TO"]);
        $this->update["STAFFCD"] = STAFFCD;
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] ="sysdate()";
        if($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $this->update["SCHOOLCD"] = SCHOOLCD;
            $this->update["SCHOOL_KIND"] = SCHOOLKIND;
        }
        common::_update(OSHIRASE_TBL, "", "", $this->update, $db, "2");
        
        if($this->selmode == ""){
            //OSHIRASE_GRPの挿入
            //先に共通のもの入れておく
            $this->grpup["DATA_DIV"] = $this->dataDiv;
            $this->grpup["OSHIRASE_NO"] = $tblMax;
            $this->grpup["REGISTERCD"] = STAFFCD;
            $this->grpup["UPDATED"] = "sysdate()";
            if($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
                $this->grpup["SCHOOLCD"] = SCHOOLCD;
                $this->grpup["SCHOOL_KIND"] = SCHOOLKIND;
            }
            
            //OSHIRASE_GRPのDATA_NOの最大値取得
            $maxQuery = knjz334Query::getMax($this, "GRP");
            $grpMax = $db->getOne($maxQuery);
            $grpMax = $grpMax + 1;

            //選択されたグループを1個ずつ更新
            foreach($this->field["SELECT_GROUP"] as $key => $val){
                $this->grpup["DATA_NO"] = $grpMax + $key;
                
                $this->grpup["GROUP_CD"] = $val;

                common::_update(OSHIRASE_GRP, "", "", $this->grpup, $db, "2");
            }
        }else{
            //OSHIRASE_INDの挿入
            $this->indup["DATA_DIV"] = $this->dataDiv;
            $this->indup["OSHIRASE_NO"] = $tblMax;
            $this->indup["REGISTERCD"] = STAFFCD;
            $this->indup["UPDATED"] = "sysdate()";
            if($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
                $this->indup["SCHOOLCD"] = SCHOOLCD;
                $this->indup["SCHOOL_KIND"] = SCHOOLKIND;
            }
            
            //OSHIRASE_INDのDATA_NOの最大値取得
            $maxQuery = knjz334Query::getMax($this, "IND");
            $indMax = $db->getOne($maxQuery);
            $indMax = $indMax + 1;
            
            //選択された個人を配列にする
            $selindiv = explode(",", $this->selectdata);

            //配列で重複している物を削除する
            $unique = array_unique($selindiv);

            //キーが飛び飛びになっているので、キーを振り直す
            $new_selindiv = array_values($unique);


            //1こずつ更新
            foreach($new_selindiv as $key => $val){
                $this->indup["DATA_NO"] = $indMax + $key;
                
                $this->indup["STAFFCD"] = $val;
                
                common::_update(OSHIRASE_IND, "", "", $this->indup, $db, "2");
            }
        }
        
        $this->setMessage("追加しました");
        
        Query::dbCheckIn($db);
        
        return true;
    }
    
    /*更新*/
    function getUpdateModel()
    {
        unset($this->update);
        unset($this->grpup);
        unset($this->indup);
        
        //GROUPが選択されてるかのエラーチェック
        if($this->selmode == ""){
            if(!is_array($this->field["SELECT_GROUP"])){
                $this->setMessage("対象のグループを選択してください");
                $this->error = "1";
                return false;
            }else{
                $this->error = "";
            }
        }else{
            if($this->selectdata == ""){
                $this->setMessage("対象の個人を選択してください");
                $this->error = "1";
                return false;
            }else{
                $this->error = "";
            }
        }
        $db = Query::dbCheckOut();
        
        //OSHIRASE_TBLの更新
        $this->update["ANNOUNCE"][TEXT]     = $this->field["ANNOUNCE"];
        $this->update["ANNOUNCE_ENG"][TEXT] = $this->field["ANNOUNCE_ENG"];
        $this->update["START_DATE"][DATE]   = str_replace("/", "-", $this->field["FROM"]);
        $this->update["END_DATE"][DATE]     = str_replace("/", "-", $this->field["TO"]);
        $this->update["ENTRY_DATE"][DATE]   = date("Y-m-d");
        $this->update["STAFFCD"][TEXT]      = STAFFCD;
        $this->update["REGISTERCD"][TEXT]   = STAFFCD;
        $this->update["UPDATED"][FUNC]      = "sysdate()";
        
        $where  = " WHERE OSHIRASE_NO = ".$this->seldata." ";
        $where .= "       AND DATA_DIV = ".$this->dataDiv." ";
        if($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            //別になくてもいい
            $where .= " AND SCHOOLCD = '".SCHOOLCD."' AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $query = Query::updateSQL($this->update, "OSHIRASE_TBL", $where);
        $db->query($query);

        if($this->selmode == ""){
            //OSHIRASE_GRPの更新
            
            //元が個人対象だったらOSHIRASE_IND削除したい
            $cntQuery = knjz334Query::getCount($this, $this->seldata, "IND");
            $cnt = $db->getOne($cntQuery);
            
            if($cnt > 0){   //OSHIRASE_INDにあったらOSHIRASE_GRPにはないはず
                $delQuery = knjz334Query::delete($this, $this->seldata, "IND");
                $db->query($delQuery);
            }else{
                $delQuery = knjz334Query::delete($this, $this->seldata, "GRP");
                $db->query($delQuery);
            }
            
            //消した後でOSHIRASE_GRPのDATA_NOの最大値を取得
            $maxQuery = knjz334Query::getMax($this, "GRP");
            $maxNo = $db->getOne($maxQuery);
            $maxNo = $maxNo + 1;
            
            //選択されたグループを1個ずつ更新
            foreach($this->field["SELECT_GROUP"] as $key => $val){
                $this->grpup["DATA_NO"] = $maxNo + $key;
                $this->grpup["DATA_DIV"] = $this->dataDiv;
                $this->grpup["OSHIRASE_NO"] = $this->seldata;
                
                $this->grpup["GROUP_CD"] = $val;
                
                $this->grpup["REGISTERCD"] = STAFFCD;
                $this->grpup["UPDATED"] = "sysdate()";
                if($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
                    $this->grpup["SCHOOLCD"] = SCHOOLCD;
                    $this->grpup["SCHOOL_KIND"] = SCHOOLKIND;
                }
                
                common::_update(OSHIRASE_GRP, "", "", $this->grpup, $db, "2");
            }
        }else{
            //OSHIRASE_INDの更新

            //元がグループ対象だった可能性があるのでOSHIRASE_GRPも削除したい
            $cntQuery = knjz334Query::getCount($this, $this->seldata, "GRP");
            $cnt = $db->getOne($cntQuery);
            
            if($cnt > 0){   //OSHIRASE_GRPにあったらOSHIRASE_INDにはないはず
                $delQuery = knjz334Query::delete($this, $this->seldata, "GRP");
                $db->query($delQuery);
            }else{
                $delQuery = knjz334Query::delete($this, $this->seldata, "IND");
                $db->query($delQuery);
            }
            
            //消した後でOSHIRASE_INDのDATA_NOの最大値を取得
            $maxQuery = knjz334Query::getMax($this, "IND");
            $maxNo = $db->getOne($maxQuery);
            $maxNo = $maxNo + 1;
            
            //選択された個人を配列にする
            $selindiv = explode(",", $this->selectdata);

            //配列で重複している物を削除する
            $unique = array_unique($selindiv);

            //キーが飛び飛びになっているので、キーを振り直す
            $new_selindiv = array_values($unique);


            //1こずつ更新
            foreach($new_selindiv as $key => $val){
                $this->indup["DATA_NO"] = $maxNo + $key;
                $this->indup["DATA_DIV"] = $this->dataDiv;
                $this->indup["OSHIRASE_NO"] = $this->seldata;
                
                $this->indup["STAFFCD"] = $val;
                $this->indup["REGISTERCD"] = STAFFCD;
                $this->indup["UPDATED"] = "sysdate()";
                if($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
                    $this->indup["SCHOOLCD"] = SCHOOLCD;
                    $this->indup["SCHOOL_KIND"] = SCHOOLKIND;
                }
                
                common::_update(OSHIRASE_IND, "", "", $this->indup, $db, "2");
            }
        }
        $this->setMessage("更新しました");
        
        Query::dbCheckIn($db);
        
        return true;
    }
    
    /*削除*/
    function getDeleteModel()
    {
        $db = Query::dbCheckOut();

        //OSHIRASE_NOでOSHIRASE_TBL, OSHIRASE_GRP, OSHIRASE_GRPを削除する
        $tblQuery = knjz334Query::delete($this, $this->seldata, "TBL");
        $db->query($tblQuery);
        
        //とりあえずOSHIRASE_GRPの件数をカウントしよう
        $cntQuery = knjz334Query::getCount($this, $this->seldata, "GRP");
        $cnt = $db->getOne($cntQuery);
        
        if($cnt > 0){
            $grpQuery = knjz334Query::delete($this, $this->seldata, "GRP");
            $db->query($grpQuery);
        }else{
            $grpQuery = knjz334Query::delete($this, $this->seldata, "IND");
            $db->query($grpQuery);
        }

        $filename = DOCUMENTROOT ."/oshiraseFile/" . $this->seldata ."/";
        system("rm -rf '{$filename}'");

        $this->setMessage("削除しました");
        
        Query::dbCheckIn($db);
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        $setFileName = explode(".", $this->file['name']);
        $extension = $setFileName[get_count($setFileName) - 1];

        if ($extension != 'pdf' && $extension != 'PDF') {
            $this->warning = "PDFファイル以外は取り込めません。";
            return false;
        }

        //フォルダ作成
        $dir = "/oshiraseFile/";
        $dataDir = DOCUMENTROOT . $dir;
        if (!is_dir($dataDir)) {
            if (!mkdir($dataDir, 0777)) {
                //作成失敗
            }
        }
        if (strlen($this->seldata) > 0) {
            $dir .= $this->seldata . "/";
            $dataDir = DOCUMENTROOT . $dir;
            if (!is_dir($dataDir)) {
                if (!mkdir($dataDir, 0777)) {
                    //作成失敗
                }
            }
        }

        $this->allDelFlg = false;
        $setFileName[0] = mb_convert_encoding($setFileName[0], "SJIS-win", "UTF-8");
        $filename = DOCUMENTROOT ."/oshiraseFile/" . $this->seldata ."/". $setFileName[0].".".$setFileName[1];

        if (!is_uploaded_file($this->file['tmp_name'])) {
            $this->warning = "ファイル名が不正です。";
        } else {
            if (move_uploaded_file($this->file['tmp_name'], $filename) == FALSE){
                $this->warning = "取込失敗です。";
            } else {
                $this->message = "取込正常です。";
            }
        }
    }

    /**
     * 実行モデルを返します
    */
    function &getExecDelModel()
    {
        $fileName = VARS::post("fileName");

        $setFileName = mb_convert_encoding($fileName, "SJIS-win", "UTF-8");
        $filename = DOCUMENTROOT ."/oshiraseFile/" . $this->seldata ."/". $setFileName;

        system("rm -f '{$filename}'");

        //ファイルが無ければフォルダ削除
        $dir = "/oshiraseFile/" . $this->seldata . "/";
        $dataDir = DOCUMENTROOT . $dir;
        $cnt = 0;
        if (!is_dir($dataDir)) {
            //フォルダなし
        } else if ($readFileName = opendir($dataDir)) {
            while (false !== ($filename = readdir($readFileName))) {
                if ($filename != "." && $filename != "..") {
                    $cnt++;
                }
            }
            closedir($readFileName);
        }
        $this->allDelFlg = false;
        if ($cnt == 0) {
            system("rm -rf '{$dataDir}'");
            $this->allDelFlg = true;
        }

        return true;
    }
}
?>
