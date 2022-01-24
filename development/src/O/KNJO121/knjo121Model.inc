<?php
require_once('for_php7.php');
class knjo121Model extends Model {
    var $grade;
    var $cmd;
    var $examyear;
    var $recordTableDiv;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjo121Model()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->valErrMess = array();
        $this->valErrFlg = "";
        $this->loginyear = CTRL_YEAR;


        $this->cmd   = VARS::request("cmd");
        $this->file = VARS::file("XML");
        
        //パーツから受け取り
        $this->field = array("SCHREGNO" => VARS::post("SCHREGNO"),  //0
                             "KATEI"    => VARS::post("KATEI"),     //1
                             "COURSE"   => VARS::post("COURSE"),    //2
                             "GRADE"    => VARS::post("GRADE"),     //3
                             "ATTENDNO" => VARS::post("ATTENDNO"),  //4
                             "ANNUAL"   => VARS::post("ANNUAL"),    //5
                             "ENTDATE"  => VARS::post("ENTDATE") != "" ? str_replace("/","-",VARS::post("ENTDATE")) : date("Y-m-d"),  //6
                             "TYPE"     => VARS::post("TYPE") != "" ? VARS::post("TYPE") : 1,   //7
                             "INTYPE"   => VARS::post("INTYPE") != "" ? VARS::post("INTYPE") : 1,   //8
                             );
        $this->radio = $this->field["TYPE"];  //7

        //パーツ作成用変数
        $this->parts = array();
        //学籍番号
        $this->parts[0] = array("TYPE"      =>  "text",
                                "NAME"      =>  "SCHREGNO",
                                "SIZE"      =>  10,
                                "MAXLENGTH" =>  8,
                                "EXTRA"     => "",
                                "LABEL"     =>  "※学籍番号");
        //課程学科用query
        $query1 = knjo121Query::getKatei();
        $this->parts[1] = array("TYPE"      =>  "combo",
                                "NAME"      =>  "KATEI",
                                "SQL"       =>  $query1,
                                "EXTRA"     =>  "",
                                "BLANK"     =>  "BLANK",
                                "SIZE"      =>  "1",
                                "LABEL"     =>  "※課程・学科");
        
        //コース用query
        $query2 = knjo121Query::getCourse();
        $this->parts[2] = array("TYPE"      =>  "combo",
                                "NAME"      =>  "COURSE",
                                "SQL"       =>  $query2,
                                "EXTRA"     =>  "",
                                "BLANK"     =>  "BLANK",
                                "SIZE"      =>  "1",
                                "LABEL"     =>  "※コース");
        
        //年組用query
        $query3 = knjo121Query::getGrade();
        $this->parts[3] = array("TYPE"      =>  "combo",
                                "NAME"      =>  "GRADE",
                                "SQL"       =>  $query3,
                                "EXTRA"     =>  "",
                                "BLANK"     =>  "BLANK",
                                "SIZE"      =>  "1",
                                "LABEL"     =>  "※年組");
        
        //出席番号
        $this->parts[4] = array("TYPE"      =>  "text",
                                "NAME"      =>  "ATTENDNO",
                                "SIZE"      =>  4,
                                "MAXLENGTH" =>  3,
                                "EXTRA"     => "",
                                "LABEL"     =>  "※番");

        //年次
        $this->parts[5] = array("TYPE"      =>  "text",
                                "NAME"      =>  "ANNUAL",
                                "SIZE"      =>  4,
                                "MAXLENGTH" =>  2,
                                "EXTRA"     => "",
                                "LABEL"     =>  "※年次");

        //日付
        $this->parts[6] = array("TYPE"      =>  "calendar",
                                "NAME"      =>  "ENTDATE",
                                "LABEL"     =>  "※入学日");
        
        //xsdラジオボタン
        $option = " onclick=\" radio_change(this);\"";
        $this->parts[7] = array("TYPE"      =>  "radio",
                                "NAME"      =>  "TYPE",
                                "VALUE"     =>  array(2, 1),
                                "EXTRA"     =>  array("id=\"TYPE1\" {$option}", "id=\"TYPE2\" {$option}"),
                                "R_LABEL"   =>  array("学習者情報", "学齢簿情報"),
                                "LABELID"   =>  array("TYPE1", "TYPE2"),
                                "LABEL"     =>  "※取込データ");
        
        //学齢簿のときのみ入学か転入か選択できるように
        if($this->radio != "1"){
            $option = "";
            $this->parts[8] = array("TYPE"      =>  "radio",
                                    "NAME"      =>  "INTYPE",
                                    "VALUE"     =>  array(2, 1),
                                    "EXTRA"     =>  array("id=\"INTYPE1\" {$option}", "id=\"INTYPE2\" {$option}"),
                                    "R_LABEL"   =>  array("入学", "転入学"),
                                    "LABELID"   =>  array("INTYPE1", "INTYPE2"),
                                    "LABEL"     =>  "※取込区分");
        }
        $this->partsCnt = get_count($this->parts);
        
        
        //ラジオボタン用の変数
        /*$this->radioName = "TYPE";
        $this->radioVal = array(2, 1);
        $option = " onclick=\" radio_change(this);\"";
        $this->radioExtra = array("id=\"TYPE1\" {$option}", "id=\"TYPE2\" {$option}");
        $this->radioLabel = array("学習者情報", "学齢簿情報");
        $this->radioLabelId = array("TYPE1", "TYPE2");
        $this->radioThLabel = "取込データ";*/
        
        //画面タイトル用
        $this->title = "{$this->loginyear}年度 学習者情報/学齢簿情報";
        
        $this->delType = VARS::post("DELTYPE");
        

    }

    function fileUpdate()
    {
        if($this->radio == "1"){
            // $this->xsd =  DOCUMENTROOT ."/common/XSD/"."eduAK0101s-2016-01.xsd";
            $this->xsd =  DOCUMENTROOT ."/common/XSD/"."eduAK0101s-2020-01.xsd";
        }else{
            // $this->xsd =  DOCUMENTROOT ."/common/XSD/"."lgxml20s-2016-01.xsd";
            $this->xsd =  DOCUMENTROOT ."/common/XSD/"."lgxml20s-2020-01.xsd";
        }

        //ファイルをアップロードする
        $setFileName = explode(".", $this->file['name']);

        $setFileName[0] = mb_convert_kana($setFileName[0],"s","UTF-8");
        $setFileName[0] = str_replace(" ", "-", $setFileName[0]);

        $setFileName[0] = mb_convert_encoding($setFileName[0], "SJIS-win", "UTF-8");
        $filename = DOCUMENTROOT ."/common/XML/". $setFileName[0].'.'.$setFileName[get_count($setFileName) - 1];

        if (!is_uploaded_file($this->file['tmp_name'])) {
            $this->warning = "ファイル名が不正です。";
            return false;
        } else {
            if (move_uploaded_file($this->file['tmp_name'], $filename) == FALSE){
                $this->warning = "取込失敗です。";
                return false;
            } else {
                //$this->message = "取込正常です。";
            }
        }
        
        $this->xmlfilename = $filename;

        //validationをする
        list($valError, $mess) = readxml($this);
        $this->valErrFlg = $valError;
        $this->valErrMess = $mess;
        
        if($this->valErrFlg != 0){
            $this->warning = "xmlファイルの形式が正しくありません";
            return false;
        }else{
            return true;
        }
    }
    
    function deleteModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        
        $delArray = array();
        //delTypeによって削除するテーブルが違う
        if($this->delType == "1"){
            $delArray = array("SCHREG_BASE_MST",
                              "SCHREG_ADDRESS_DAT",
                              "SCHREG_REGD_DAT",
                              "SCHREG_ENT_GRD_HIST_DAT",
                              "GUARDIAN_DAT",
                              "GUARDIAN_ADDRESS_DAT",
                              "GUARDIAN2_DAT",
                              "GUARDIAN2_ADDRESS_DAT",
                              "CE_APP_YOUROKU_FROM_MST",
                              "CE_APP_YOUROKU_IDOU_MST",
                              "CE_APP_YOUROKU_SYUBETU_MST",
                              "CE_APP_YOUROKU_TO_MST",
                              "CE_APP_Y_ALIAS_DAT",
                              "CE_APP_Y_ALL_ACT_DAT",
                              "CE_APP_Y_BACKGROUND_DAT",
                              "CE_APP_Y_BEHAVIOR_DAT",
                              "CE_APP_Y_FOR_LANG_DAT",
                              "CE_APP_Y_GRADE_DAT",
                              "CE_APP_Y_HRTEACHER_DAT",
                              "CE_APP_Y_INTEG_STUDY_DAT",
                              "CE_APP_Y_INTEG_STUDY_POINT_DAT",
                              "CE_APP_Y_OTHERADDR_DAT",
                              "CE_APP_Y_PARENT_DAT",
                              "CE_APP_Y_PARENT_OTHERADDR_DAT",
                              "CE_APP_Y_PRINCIPAL_DAT",
                              "CE_APP_Y_SCHOOL_CHANGE_DAT",
                              "CE_APP_Y_SHIDOU_DAT",
                              "CE_APP_Y_SP_ACT_DAT",
                              "CE_APP_Y_SUBJ_POINT_DAT",
                              "CE_APP_Y_SUBJ_SCORE_DAT",
                              );
        }else if($this->delType == "2"){
            $delArray = array("SCHREG_BASE_MST",
                              "SCHREG_REGD_DAT",
                              "SCHREG_ADDRESS_DAT",
                              "SCHREG_ENT_GRD_HIST_DAT",
                              "GUARDIAN_DAT",
                              "GUARDIAN_ADDRESS_DAT",
                              "GUARDIAN2_DAT",
                              "GUARDIAN2_ADDRESS_DAT",
                              "CE_APP_GAKUREIBO_F_MST",
                              "CE_APP_GAKUREIBO_S_MST",
                              "CE_APP_G_CHANGE_DAT",
                              "CE_APP_G_JNR_DEMAND_DAT",
                              "CE_APP_G_JNR_EXTRA_DAT",
                              "CE_APP_G_JNR_OUTSIDE_DAT",
                              "CE_APP_G_JNR_POST_DAT",
                              "CE_APP_G_PRI_DEMAND_DAT",
                              "CE_APP_G_PRI_EXTRA_DAT",
                              "CE_APP_G_PRI_OUTSIDE_DAT",
                              "CE_APP_G_PRI_POST_DAT",
                              );
        }else{
            $delArray = array("SCHREG_BASE_MST",
                              "SCHREG_ADDRESS_DAT",
                              "SCHREG_REGD_DAT",
                              "SCHREG_ENT_GRD_HIST_DAT",
                              "GUARDIAN_DAT",
                              "GUARDIAN_ADDRESS_DAT",
                              "GUARDIAN2_DAT",
                              "GUARDIAN2_ADDRESS_DAT",
                              );
        }
        
        //要録取り込んだテーブルすべて
        foreach($delArray as $val){
            $query = knjo121Query::delete($this->field["SCHREGNO"], $val);
            $db->query($query);
        }
        //SCHOOLCDが1のもののみ削除のテーブル
        $delsArray = array("SCHREG_ATTENDREC_DAT",
                          );
        foreach($delsArray as $val){
            $query = knjo121Query::delete2($this->field["SCHREGNO"], $val);
            $db->query($query);
        }
        
        Query::dbCheckIn($db);
    }
    
    function updateModel()
    {
        
        //データを配列に
        $data = getReadModel($this);

        //データ更新function
        if($this->radio == "1"){
            ceYourokuUpdate($this->field["SCHREGNO"], $data);
        }else{
            ceGakureiboUpdate($this->field["SCHREGNO"], $data);
        }

        //とりあえずecho
        //print_r($data);
        return true;
    }    

    function insertModel()
    {
        //賢者用データ作成Model
        if($this->radio == "1"){
            $error = kenjaInsertShidou($this->field);
        }else{
            $error = kenjaInsertGaku($this->field);
        }
        
        if($error != 0){
            $this->setMessage("取込データのコード値に不備があるため、\\n学籍データを作成できませんでした。");
        }else{
            $this->setMessage("更新しました。");
        }
        return true;
    }

    function validateCode()
    {
        $valiErrFlg = true;

        if($this->radio == "1"){
            //データを配列に
            $valiData = getReadModel($this);

            // バリデーション実行
            $valiErrFlg = xmlArrayValidation($valiData);
        }

        if(!$valiErrFlg){
            $this->warning = "取込データのコード値に不備があるため、\\n学習者情報を取り込みできませんでした。";
            return false;
        }else{
            return true;
        }
    }
}

    // 取込データのバリデーション
    function xmlArrayValidation($data) 
    {
        //DB接続
        $db = Query::dbCheckOut();

        // 学年
        //コード辞書から学年のコードを取得
        $applicGrade = array();
        $query = knjo121Query::getApplicGrade();
        $result = $db->query($query);
        while($gradeRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $applicGrade[] = $gradeRow["DICTIONARY_CODE"];
        }
        Query::dbCheckIn($db);

        // 学校変更状況→学年
        $list_changeGradeCd = array_column($data["指導要録情報"]["学校変更状況"], '学年');
        if($list_changeGradeCd == NULL) {
            $list_changeGradeCd[0] = $data["指導要録情報"]["学校変更状況"]["学年"];
        }
        if(!checkDictionaryCode($applicGrade, $list_changeGradeCd)) {
            return false;
        }
        
        // 在学状況→在学学年
        $list_zaigakuGradeCd = array_column($data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"], '学年');
        if($list_zaigakuGradeCd == NULL) {
            $list_zaigakuGradeCd[0] = $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"]["学年"];
        }

        // 重複のチェック
        $value_count = array_count_values($list_zaigakuGradeCd); // 各値の出現回数を数える
        $max = max($value_count); // 最大の出現回数を取得する
        if ($max > 1) {
            return false;
        }

        if(!checkDictionaryCode($applicGrade, $list_zaigakuGradeCd)) {
            return false;
        }

        // 異動状況→編入学時学年
        if(in_array('編入学', $data["指導要録情報"]["学籍に関する記録"]["異動状況"])) {
            $list_hennyuGradeCd = array_column($data["指導要録情報"]["学籍に関する記録"]["異動状況"]["編入学"],'編入学時学年');
            if($list_hennyuGradeCd == NULL) {
                $list_hennyuGradeCd[0] = $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["編入学"]["編入学時学年"];
            }
            if(!checkDictionaryCode($applicGrade, $list_hennyuGradeCd)) {
                return false;
            }
        }

        // 異動状況→転入学時学年
        if(in_array('転入学', $data["指導要録情報"]["学籍に関する記録"]["異動状況"])) {
            $list_tennyuGradeCd = array_column($data["指導要録情報"]["学籍に関する記録"]["異動状況"]["転入学"], '転入学時学年');
            if($list_tennyuGradeCd == NULL) {
                $list_tennyuGradeCd[0] = $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["転入学"]["転入学時学年"];
            }
            if(!checkDictionaryCode($applicGrade, $list_tennyuGradeCd)) {
                return false;
            }
        }
        
        // 指導に関する記録→学年
        $list_shidouGradeCd = array_column($data["指導要録情報"]["指導に関する記録"], '学年');
        if($list_shidouGradeCd == NULL) {
            $list_shidouGradeCd[0] = $data["指導要録情報"]["指導に関する記録"]["学年"];
        }

        if(!checkDictionaryCode($applicGrade, $list_shidouGradeCd) || !checkDictionaryCode($list_zaigakuGradeCd, $list_shidouGradeCd)) {
            return false;
        }

        return true;
    }
    
// コードを照らし合わせてチェック
function checkDictionaryCode($dictionarycode, $data) {
    foreach($data as $key => $val){

        if(!in_array($val, $dictionarycode)) {
            return false;
        }
    }
    return true;
}

//学習者情報(CE_APP_YOUROKU_テーブル)の更新
function ceYourokuUpdate($schregNo, $data) 
{
    //更新用変数
    $yourokuTo = array();   //CE_APP_YOUROKU_TO_MST更新用
    $yourokuFrom = array(); //CE_APP_YOUROKU_FROM_MST更新用
    $syubetu = array();     //CE_APP_YOUROKU_SYUBETU_MST更新用
    $idou = array();        //CE_APP_YOUROKU_IDOU_MST更新用
    
    //とりあえず学籍番号適当に
    $yourokuTo["SCHREGNO"] = $schregNo;
    $yourokuTo["REGISTERCD"] = STAFFCD;
    $yourokuTo["UPDATED"] = "sysdate()";
    $yourokuFrom["SCHREGNO"] = $schregNo;
    $yourokuFrom["REGISTERCD"] = STAFFCD;
    $yourokuFrom["UPDATED"] = "sysdate()";
    $syubetu["SCHREGNO"] = $schregNo;
    $syubetu["REGISTERCD"] = STAFFCD;
    $syubetu["UPDATED"] = "sysdate()";
    $idou["SCHREGNO"] = $schregNo;
    $idou["REGISTERCD"] = STAFFCD;
    $idou["UPDATED"] = "sysdate()";
    
    //変換用配列   XMLから作成した配列のKEY => 更新するテーブルのフィールド名
    //CE_APP_YOUROKU_TO_MST/CE_APP_YOUROKU_FROM_MST共通
    $fromTo = array("教育委員会ID"      => "ID",
                    "外字教育委員会名"  => "GNAME",
                    "内字教育委員会名"  => "NNAME",
                    "学校ID"            => "ID",
                    "外字学校名"        => "GNAME",
                    "内字学校名"        => "NNAME",
                    "部課係"            => "SECTION",
                    "住所コード"        => "ADDRCD",
                    "外字住所"          => "GADDR",
                    "内字住所"          => "NADDR",
                    "外字方書"          => "GKATAGAKI",
                    "内字方書"          => "NKATAGAKI",
                    "郵便番号"          => "ZIPCD",
                    "電話番号"          => "TELNO",
                    "職"                => "SYOKU",
                    "分校ID"            => "BUN_ID",
                    "外字分校名"        => "BUN_GNAME",
                    "内字分校名"        => "BUN_NNAME",
                    "備考"              => "BIKOU");
                    
    $Bunko = array("住所コード" => "BUN_ADDRCD",
                   "外字住所"   => "BUN_GADDR",
                   "内字住所"   => "BUN_NADDR",
                   "外字方書"   => "BUN_GKATAGAKI",
                   "内字方書"   => "BUN_NKATAGAKI",
                   "郵便番号"   => "BUN_ZIPCD");
                    
    
    //氏名・通称名用
    $Name = array("外字氏名" => "GNAME",
                  "内字氏名" => "NNAME",
                  "フリガナ" => "NAME_KANA");
    
    //住所用
    $Addr = array("住所コード"  => "ADDRCD",
                  "外字住所"    => "GADDR",
                  "内字住所"    => "NADDR",
                  "外字方書"    => "GKATAGAKI",
                  "内字方書"    => "NKATAGAKI",
                  "郵便番号"    => "ZIPCD");
                  
    
    //DB接続
    $db = Query::dbCheckOut();

    foreach($data as $yKey => $yVal){
        if($yKey == "利用業務ユニット"){
            
            //CE_APP_YOUROKU_TO_MST
            $yourokuTo["GYOUMU_UNIT"] = $yVal;
            
        }else if($yKey == "送信先情報"){
            foreach($yVal as $fKey => $fVal){
                if($fKey == "教育委員会"){
                    $yourokuTo["INFO_FLG"] = 1;
                }else{
                    $yourokuTo["INFO_FLG"] = 2;
                }
                
                foreach($fVal as $sKey => $sVal){
                    if(!is_array($sVal)){
                        $yourokuTo[$fromTo[$sKey]] = $sVal;
                    }else{
                        if($sKey != "分校所在地"){
                            foreach($sVal as $tKey => $tVal){
                                $yourokuTo[$fromTo[$tKey]] = $tVal;
                            }
                        }else{
                            foreach($sVal as $tKey => $tVal){
                                $yourokuTo[$Bunko[$tKey]] = $tVal;
                            }
                        }
                    }
                }
            }
            
        }else if($yKey == "送信元情報"){
            foreach($yVal as $fKey => $fVal){
                if($fKey == "教育委員会"){
                    $yourokuFrom["INFO_FLG"] = 1;
                }else{
                    $yourokuFrom["INFO_FLG"] = 2;
                }
                
                foreach($fVal as $sKey => $sVal){
                    if(!is_array($sVal)){
                        $yourokuFrom[$fromTo[$sKey]] = $sVal;
                    }else{
                        if($sKey != "分校所在地"){
                            foreach($sVal as $tKey => $tVal){
                                $yourokuFrom[$fromTo[$tKey]] = $tVal;
                            }
                        }else{
                            foreach($sVal as $tKey => $tVal){
                                $yourokuFrom[$Bunko[$tKey]] = $tVal;
                            }
                        }
                    }
                }
            }
        }else if($yKey == "送信日時"){
            
            //Tを半角スペースに変換
            $send = str_replace("T", " ", $yVal);
            $send = explode("+", $send);
            $send_date = date("Y-m-d H:i:s", strtotime($send[0]));

            // preg_match('/(?<=\.)[0-9]*(?=\+)/', $send, $send_msc);
            // if(mb_strlen($send_msc[0]) > 6 ){
            //     preg_match('/^.*(?=[0-9]\+)/',$send, $send_date);
            // } else {
            //     //+から上だけ使う
            //     $send_date = explode("+", $send);
            // }

            //CE_APP_YOUROKU_FROM_MST
            //$yourokuFrom["SEND_DATE"] = "'".$send_date[0].".000000'";
            $yourokuFrom["SEND_DATE"] = "'$send_date'";

        }else{  //指導要録情報
            foreach($yVal as $fKey => $fVal){
                if($fKey == "指導要録種別"){
                    //CE_APP_YOUROKU_SYUBETU_MST
                    $syubetu["SYUBETU"] = $fVal;
                    
                }else if($fKey == "学校変更状況"){
                    $changeArray = array("年度" => "YEAR",
                                         "学年" => "GRADE",
                                         "参考様式_版" => "STYLE_VER");
                    
                    $changeCnt = 1;
                    $change = array();
                    $change["REGISTERCD"] = STAFFCD;
                    $change["UPDATED"] = "sysdate()";
                    $change["SCHREGNO"] = $schregNo;
                    
                    foreach($fVal as $sKey => $sVal){
                        if(is_numeric($sKey)){
                            $changeUpdate = 0;
                            
                            $change["DATA_ROW"] = $changeCnt;
                            
                            //校長氏名
                            $princeCnt = 1;
                            $prince = array();
                            $prince["REGISTERCD"] = STAFFCD;
                            $prince["UPDATED"] = "sysdate()";
                            $prince["SCHREGNO"] = $schregNo;
                            $prince["UPPER_DATA_ROW"] = $changeCnt;
                            
                            //学級担任者氏名
                            $hrCnt = 1;
                            $hrTeacher = array();
                            $hrTeacher["REGISTERCD"] = STAFFCD;
                            $hrTeacher["UPDATED"] = "sysdate()";
                            $hrTeacher["SCHREGNO"] = $schregNo;
                            $hrTeacher["UPPER_DATA_ROW"] = $changeCnt;

                            foreach($sVal as $tKey => $tVal){
                                if(array_key_exists($tKey, $changeArray)){
                                    $change[$changeArray[$tKey]] = $tVal;
                                }else{
                                    if($tKey == "校長氏名"){
                                        foreach($tVal as $rKey => $rVal){
                                            if(is_numeric($rKey)){
                                                $princeUpdate = 0;
                                                $prince["DATA_ROW"] = $princeCnt;
                                                
                                                foreach($rVal as $vKey => $vVal){
                                                    $prince[$Name[$vKey]] = $vVal;
                                                }
                                                _update(CE_APP_Y_PRINCIPAL_DAT, "", "", $prince, $db, "2");
                                                
                                                $princeCnt++;
                                            }else{
                                                $princeUpdate = 1;
                                                $prince[$Name[$rKey]] = $rVal;
                                            }
                                        }
                                        if($princeUpdate != 0){
                                            $prince["DATA_ROW"] = $princeCnt;
                                            _update(CE_APP_Y_PRINCIPAL_DAT, "", "", $prince, $db, "2");
                                        }
                                    }else{
                                        foreach($tVal as $rKey => $rVal){
                                            if(is_numeric($rKey)){
                                                $hrUpdate = 0;
                                                $hrTeacher["DATA_ROW"] = $hrCnt;
                                                
                                                foreach($rVal as $vKey => $vVal){
                                                    $hrTeacher[$Name[$vKey]] = $vVal;
                                                }
                                                _update(CE_APP_Y_HRTEACHER_DAT, "", "", $hrTeacher, $db, "2");
                                                
                                                $hrCnt++;
                                            }else{
                                                $hrUpdate = 1;
                                                $hrTeacher[$Name[$rKey]] = $rVal;
                                            }
                                        }
                                        if($hrUpdate != 0){
                                            $hrTeacher["DATA_ROW"] = $hrCnt;
                                            _update(CE_APP_Y_HRTEACHER_DAT, "", "", $hrTeacher, $db, "2");
                                        }
                                    }
                                }
                            }

                            _update(CE_APP_Y_SCHOOL_CHANGE_DAT, "", "", $change, $db, "2");
                            
                            $changeCnt++;
                            
                        }else{
                            $changeUpdate = 1;
                            
                            //校長氏名
                            $princeCnt = 1;
                            $prince = array();
                            $prince["REGISTERCD"] = STAFFCD;
                            $prince["UPDATED"] = "sysdate()";
                            $prince["SCHREGNO"] = $schregNo;
                            $prince["UPPER_DATA_ROW"] = $changeCnt;
                            
                            //学級担任者氏名
                            $hrCnt = 1;
                            $hrTeacher = array();
                            $hrTeacher["REGISTERCD"] = STAFFCD;
                            $hrTeacher["UPDATED"] = "sysdate()";
                            $hrTeacher["SCHREGNO"] = $schregNo;
                            $hrTeacher["UPPER_DATA_ROW"] = $changeCnt;

                            if(array_key_exists($sKey, $changeArray)){
                                $change[$changeArray[$sKey]] = $sVal;
                            }else{
                                if($sKey == "校長氏名"){
                                    foreach($sVal as $tKey => $tVal){
                                        if(is_numeric($tKey)){
                                            $princeUpdate = 0;
                                            $prince["DATA_ROW"] = $princeCnt;
                                            
                                            foreach($tVal as $rKey => $rVal){
                                                $prince[$Name[$rKey]] = $rVal;
                                            }
                                            _update(CE_APP_Y_PRINCIPAL_DAT, "", "", $prince, $db, "2");
                                            
                                            $princeCnt++;
                                        }else{
                                            $princeUpdate = 1;
                                            $prince[$Name[$tKey]] = $tVal;
                                        }
                                    }
                                    if($princeUpdate != 0){
                                        $prince["DATA_ROW"] = $princeCnt;

                                        _update(CE_APP_Y_PRINCIPAL_DAT, "", "", $prince, $db, "2");
                                    }
                                }else{
                                    foreach($sVal as $tKey => $tVal){
                                        if(is_numeric($tKey)){
                                            $hrUpdate = 0;
                                            $hrTeacher["DATA_ROW"] = $hrCnt;
                                            
                                            foreach($tVal as $rKey => $rVal){
                                                $hrTeacher[$Name[$rKey]] = $rVal;
                                            }
                                            _update(CE_APP_Y_HRTEACHER_DAT, "", "", $hrTeacher, $db, "2");
                                            
                                            $hrCnt++;
                                        }else{
                                            $hrUpdate = 1;
                                            $hrTeacher[$Name[$tKey]] = $tVal;
                                        }
                                    }

                                    if($hrUpdate != 0){
                                        $hrTeacher["DATA_ROW"] = $hrCnt;
                                        _update(CE_APP_Y_HRTEACHER_DAT, "", "", $hrTeacher, $db, "2");
                                    }
                                }
                            }
                        }
                    }

                    if($changeUpdate != 0){
                        $change["DATA_ROW"] = $changeCnt;
                        
                        _update(CE_APP_Y_SCHOOL_CHANGE_DAT, "", "", $change, $db, "2");
                    }
                }else if($fKey == "学籍に関する記録"){
                    
                    foreach($fVal as $sKey => $sVal){
                        if($sKey == "児童生徒"){
                            //変換用
                            $student = array("児童生徒ID"   => "STUDENT_ID",
                                             "性別"         => "SEX",
                                             "生年月日"     => "BIRTHDAY",
                                             "国籍"         => "NATION");
                                             
                            
                            foreach($sVal as $tKey => $tVal){
                                if(array_key_exists($tKey, $student)){
                                    $syubetu[$student[$tKey]] = $tVal;
                                }else if($tKey == "氏名"){
                                    foreach($tVal as $rKey => $rVal){
                                        $syubetu[$Name[$rKey]] = $rVal;
                                    }
                                }else if($tKey == "通称名"){
                                    $aliasCnt = 1;
                                    $alias = array();
                                    $alias["REGISTERCD"] = STAFFCD;
                                    $alias["UPDATED"] = "sysdate()";
                                    $alias["SCHREGNO"] = $schregNo;
                                    
                                    foreach($tVal as $rKey => $rVal){
                                        if(is_numeric($rKey)){
                                            $aliasUpdate = 0;
                                            $alias["DATA_ROW"] = $aliasCnt;
                                            
                                            foreach($rVal as $vKey => $vVal){
                                                $alias[$Name[$vKey]] = $vVal;
                                            }
                                            _update(CE_APP_Y_ALIAS_DAT, "", "", $alias, $db, "2");
                                            $aliasCnt++;
                                            
                                        }else{
                                            $aliasUpdate = 1;
                                            
                                            $alias[$Name[$rKey]] = $rVal;
                                        }
                                    }
                                    if($aliasUpdate != 0){
                                        $alias["DATA_ROW"] = $aliasCnt;
                                        _update(CE_APP_Y_ALIAS_DAT, "", "", $alias, $db, "2");
                                    }
                                }else if($tKey == "現住所"){
                                    foreach($tVal as $rKey => $rVal){
                                        if($rKey == "住所"){
                                            foreach($rVal as $vKey => $vVal){
                                                $syubetu[$Addr[$vKey]] = $vVal;
                                            }
                                        }else{
                                            $syubetu["ADDR_BIKOU"] = $rVal;
                                        }
                                    }
                                }else if($tKey == "現住所_その他"){
                                    $otrAddrCnt = 1;
                                    $otrAdd = array();
                                    $otrAdd["REGISTERCD"] = STAFFCD;
                                    $otrAdd["UPDATED"] = "sysdate()";
                                    $otrAdd["SCHREGNO"] = $schregNo;
                                    
                                    foreach($tVal as $rKey => $rVal){
                                        if(is_numeric($rKey)){
                                            $otrUpdate = 0;
                                            $otrAdd["DATA_ROW"] = $otrAddrCnt;
                                            
                                            foreach($rVal as $vKey => $vVal){
                                                if($vKey == "住所"){
                                                    foreach($vVal as $xKey => $xVal){
                                                        $otrAdd[$Addr[$xKey]] = $xVal;
                                                    }
                                                }else{
                                                    $otrAdd["BIKOU"] = $vVal;
                                                }
                                            }
                                            _update(CE_APP_Y_OTHERADDR_DAT, "", "", $otrAdd, $db, "2");
                                            $otrAddrCnt++;
                                            
                                        }else{
                                            $otrUpdate = 1;
                                            if($rKey == "住所"){
                                                foreach($rVal as $vKey => $vVal){
                                                    $otrAdd[$Addr[$vKey]] = $vVal;
                                                }
                                            }else{
                                                $otrAdd["BIKOU"] = $rVal;
                                            }
                                        }
                                    }
                                    if($otrUpdate != 0){
                                        $otrAdd["DATA_ROW"] = $otrAddrCnt;
                                        _update(CE_APP_Y_OTHERADDR_DAT, "", "", $otrAdd, $db, "2");
                                    }
                                }else if($tKey == "保護者"){
                                    $parentCnt = 1;
                                    $parent = array();
                                    $parent["REGISTERCD"] = STAFFCD;
                                    $parent["UPDATED"] = "sysdate()";
                                    $parent["SCHREGNO"] = $schregNo;
                                    
                                    foreach($tVal as $rKey => $rVal){
                                        if(is_numeric($rKey)){
                                            $parentUpdate = 0;
                                            $parent["DATA_ROW"] = $parentCnt;
                                            
                                            $parOtrAddCnt = 1;
                                            $parOtrAdd = array();
                                            $parOtrAdd["REGISTERCD"] = STAFFCD;
                                            $parOtrAdd["UPDATED"] = "sysdate()";
                                            $parOtrAdd["SCHREGNO"] = $schregNo;
                                            $parOtrAdd["UPPER_DATA_ROW"] = $parentCnt;
                                            
                                            foreach($rVal as $vKey => $vVal){
                                                if($vKey == "保護者ID"){
                                                    $parent["ID"] = $vVal;
                                                }else if($vKey == "現住所_その他"){
                                                    foreach($vVal as $xKey => $xVal){
                                                        if(is_numeric($xKey)){
                                                            $parOtrUpdate = 0;
                                                            $parOtrAdd["DATA_ROW"] = $parOtrAddCnt;
                                                            
                                                            foreach($xVal as $nKey => $nVal){
                                                                if($nKey == "住所"){
                                                                    foreach($nVal as $eKey => $eVal){
                                                                        $parOtrAdd[$Addr[$eKey]] = $eVal;
                                                                    }
                                                                }else{
                                                                    $parOtrAdd["BIKOU"] = $nVal;
                                                                }
                                                            }
                                                            _update(CE_APP_Y_PARENT_OTHERADDR_DAT, "", "", $parOtrAdd, $db, "2");
                                                            //クリア
                                                            $parOtrAdd["BIKOU"] = "";
                                                            
                                                            $parOtrAddCnt++;
                                                            
                                                        }else{
                                                            $parOtrUpdate = 1;
                                                            
                                                            if($xKey == "住所"){
                                                                foreach($xVal as $nKey => $nVal){
                                                                    $parOtrAdd[$Addr[$nKey]] = $nVal;
                                                                }
                                                            }else{
                                                                $parOtrAdd["BIKOU"] = $xVal;
                                                            }
                                                        }
                                                    }
                                                    if($parOtrUpdate != 0){
                                                        $parOtrAdd["DATA_ROW"] = $parOtrAddCnt;
                                                        _update(CE_APP_Y_PARENT_OTHERADDR_DAT, "", "", $parOtrAdd, $db, "2");
                                                    }
                                                }else{
                                                    foreach($vVal as $xKey => $xVal){
                                                        if($xKey == "住所"){
                                                            foreach($xVal as $nKey => $nVal){
                                                                $parent[$Addr[$nKey]] = $nVal;
                                                            }
                                                        }else if($xKey == "備考"){
                                                            $parent["BIKOU"] = $xVal;
                                                        }else{
                                                            $parent[$Name[$xKey]] = $xVal;
                                                        }
                                                    }
                                                }
                                            }
                                            _update(CE_APP_Y_PARENT_DAT, "", "", $parent, $db, "2");
                                            
                                            //クリア
                                            $parent["ID"] = "";
                                            $parent["BIKOU"] = "";
                                            
                                            $parentCnt++;
                                            
                                        }else{
                                            $parentUpdate = 1;
                                            
                                            $parOtrAddCnt = 1;
                                            $parOtrAdd = array();
                                            $parOtrAdd["REGISTERCD"] = STAFFCD;
                                            $parOtrAdd["UPDATED"] = "sysdate()";
                                            $parOtrAdd["SCHREGNO"] = $schregNo;
                                            $parOtrAdd["UPPER_DATA_ROW"] = $parentCnt;
                                            
                                            if($rKey == "保護者ID"){
                                                $parent["ID"] = $rVal;
                                            }else if($rKey == "現住所_その他"){
                                                foreach($rVal as $vKey => $vVal){
                                                    if(is_numeric($vKey)){
                                                        $parOtrUpdate = 0;
                                                        
                                                        $parOtrAdd["DATA_ROW"] = $parOtrAddCnt;
                                                        
                                                        foreach($vVal as $xKey => $xVal){
                                                            if($xKey == "住所"){
                                                                foreach($xVal as $nKey => $nVal){
                                                                    $parOtrAdd[$Addr[$nKey]] = $nVal;
                                                                }
                                                            }else{
                                                                $parOtrAdd["BIKOU"] = $xVal;
                                                            }
                                                        }
                                                        _update(CE_APP_Y_PARENT_OTHERADDR_DAT, "", "", $parOtrAdd, $db, "2");
                                                        //クリア
                                                        $parOtrAdd["BIKOU"] = "";
                                                        
                                                        $parOtrAddCnt++;
                                                        
                                                    }else{
                                                        $parOtrUpdate = 1;
                                                        
                                                        if($vKey == "住所"){
                                                            foreach($vVal as $xKey => $xVal){
                                                                $parOtrAdd[$Addr[$xKey]] = $xVal;
                                                            }
                                                        }else{
                                                            $parOtrAdd["BIKOU"] = $vVal;
                                                        }
                                                    }
                                                }
                                                if($parOtrUpdate != 0){
                                                    $parOtrAdd["DATA_ROW"] = $parOtrAddCnt;
                                                    
                                                    _update(CE_APP_Y_PARENT_OTHERADDR_DAT, "", "", $parOtrAdd, $db, "2");
                                                }
                                            }else{
                                                foreach($rVal as $vKey => $vVal){
                                                    if($vKey == "住所"){
                                                        foreach($vVal as $xKey => $xVal){
                                                            $parent[$Addr[$xKey]] = $xVal;
                                                        }
                                                    }else if($vKey == "備考"){
                                                        $parent["BIKOU"] = $vVal;
                                                    }else{
                                                        $parent[$Name[$vKey]] = $vVal;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if($parentUpdate != 0){
                                        $parent["DATA_ROW"] = $parentCnt;
                                        _update(CE_APP_Y_PARENT_DAT, "", "", $parent, $db, "2");
                                    }
                                }
                            }
                            
                        }else if($sKey == "在学状況"){
                            foreach($sVal as $tKey => $tVal){
                                if($tKey == "学校情報"){
                                    $school = array("学校ID"        => "SCHOOL_ID",
                                                    "外字学校名"    => "SCHOOL_GNAME",
                                                    "内字学校名"    => "SCHOOL_NNAME",
                                                    "分校ID"        => "SCHOOL_BUN_ID",
                                                    "外字分校名"    => "SCHOOL_BUN_GNAME",
                                                    "内字分校名"    => "SCHOOL_BUN_NNAME",
                                                    "電話番号"      => "SCHOOL_TELNO",
                                                    "職"            => "SCHOOL_SYOKU",
                                                    "備考"          => "SCHOOL_BIKOU");
                                                    
                                    foreach($tVal as $rKey => $rVal){
                                        if(array_key_exists($rKey, $school)){
                                            $syubetu[$school[$rKey]] = $rVal;
                                        }else{
                                            if($rKey == "所在地"){
                                                $top = "SCHOOL_";
                                            }else{
                                                $top = "SCHOOL_BUN_";
                                            }
                                            foreach($rVal as $vKey => $vVal){
                                                $syubetu[$top.$Addr[$vKey]] = $vVal;
                                            }
                                        }
                                    }
                                }else{  //在学学年
                                    $gradeChange = array("年度"     => "YEAR",
                                                         "学年"     => "GRADE",
                                                         "学級"     => "HR_CLASS",
                                                         "整理番号" => "ATTENDNO");
                                    
                                    $gradeCnt = 1;
                                    $grade = array();
                                    $grade["REGISTERCD"] = STAFFCD;
                                    $grade["UPDATED"] = "sysdate()";
                                    $grade["SCHREGNO"] = $schregNo;
                                    
                                    foreach($tVal as $rKey => $rVal){
                                        if(is_numeric($rKey)){
                                            $gradeUpdate = 0;
                                            $grade["DATA_ROW"] = $gradeCnt;
                                            
                                            foreach($rVal as $vKey => $vVal){
                                                $grade[$gradeChange[$vKey]] = $vVal;
                                            }
                                            _update(CE_APP_Y_GRADE_DAT, "", "", $grade, $db, "2");
                                            $gradeCnt++;
                                            
                                        }else{
                                            $gradeUpdate = 1;
                                            $grade[$gradeChange[$rKey]] = $rVal;
                                        }
                                    }
                                    if($gradeUpdate != 0){
                                        $grade["DATA_ROW"] = $gradeCnt;
                                        
                                        _update(CE_APP_Y_GRADE_DAT, "", "", $grade, $db, "2");
                                    }
                                }
                            }
                        }else if($sKey == "異動状況"){
                          
                            $keirekiCnt = 1;
                            $keireki = array();
                            $keireki["REGISTERCD"] = STAFFCD;
                            $keireki["UPDATED"] = "sysdate()";
                            $keireki["SCHREGNO"] = $schregNo;
                            
                            foreach($sVal as $tKey => $tVal){
                                if($tKey == "入学前の経歴"){
                                    if(is_array($tVal) && !empty($tVal)){
                                        foreach($tVal as $rKey => $rVal){
                                            $keireki["DATA_ROW"] = $keirekiCnt;
                                            $keireki["KEIREKI"] = $rVal;
                                            _update(CE_APP_Y_BACKGROUND_DAT, "", "", $keireki, $db, "2");
                                            $keirekiCnt++;
                                        }
                                    }else{
                                        $keireki["DATA_ROW"] = $keirekiCnt;
                                        $keireki["KEIREKI"] = $tVal;
                                        
                                        _update(CE_APP_Y_BACKGROUND_DAT, "", "", $keireki, $db, "2");
                                    }
                                }else if($tKey == "進学先就職先等"){
                                    $idou["AFTER_LEAVE"] = $tVal;
                                }else{
                                    if(mb_substr($tKey, -2) == "入学"){
                                        if($tKey == "入学"){
                                            $idou["IN_FLG"] = 1;
                                            $inArray = array(
                                             "入学年月日"   => "IN_DATE",
                                             "備考"         => "IN_BIKOU");

                                        }else if($tKey == "編入学"){
                                            $idou["IN_FLG"] = 2;
                                            $inArray = array(
                                             "編入学年月日"   => "IN_DATE",
                                             "編入学時学年"   => "IN_GRADE",
                                             "備考"         => "IN_BIKOU");

                                        }else{
                                            $idou["IN_FLG"] = 3;
                                            $inArray = array(
                                             "転入学年月日"   => "IN_DATE",
                                             "転入学時学年"   => "IN_GRADE",
                                             "備考"         => "IN_BIKOU");

                                        }
                                        
                                        foreach($tVal as $rKey => $rVal){
                                            $idou[$inArray[$rKey]] = $rVal;
                                        }
                                    }else{
                                        if($tKey == "転学"){
                                            $idou["OUT_FLG"] = 1;
                                            $outArray = array(
                                            "転学年月日"      => "OUT_DATE",
                                            "学校を去った日"  => "LEAVE_DATE",
                                            "備考"            => "OUT_BIKOU");

                                        }else if($tKey == "退学"){
                                            $idou["OUT_FLG"] = 2;
                                            $outArray = array(
                                            "退学年月日"      => "OUT_DATE",
                                            "備考"            => "OUT_BIKOU");
                                            
                                        }else{
                                            $idou["OUT_FLG"] = 3;
                                            $outArray = array(
                                            "卒業年月日"      => "OUT_DATE",
                                            "備考"            => "OUT_BIKOU");
                                        }

                                        foreach($tVal as $rKey => $rVal){
                                            $idou[$outArray[$rKey]] = $rVal;
                                        }
                                    }
                                }
                            }
                        }else if($sKey == "備考"){
                            $idou["BIKOU"] = $sVal;
                        }
                    }
                    
                }else if($fKey == "指導に関する記録"){
                    $shidouChange = array("学年"                        => "GRADE",
                                          "様式種別"                    => "SYUBETU",
                                          "自立の状況"                  => "JIRITU_ACT",
                                          "事項_事実_所見"              => "GENERAL_FINDINGS",
                                          "授業日数"                    => "CLASS_CNT",
                                          "出席停止_忌引等の日数"       => "STOP_CNT",
                                          "出席しなければならない日数"  => "MUST_CNT",
                                          "欠席日数"                    => "ABSENCE_CNT",
                                          "出席日数"                    => "ATTEND_CNT",
                                          "備考"                        => "BIKOU");
                    
                    $shidouCnt = 1;
                    $shidou = array();
                    $shidou["REGISTERCD"] = STAFFCD;
                    $shidou["UPDATED"] = "sysdate()";
                    $shidou["SCHREGNO"] = $schregNo;
                                          
                    foreach($fVal as $sKey => $sVal){
                        if(is_numeric($sKey)){
                            $shidouUpdate = 0;
                            
                            $shidou["REGISTERCD"] = STAFFCD;
                            $shidou["UPDATED"] = "sysdate()";
                            $shidou["SCHREGNO"] = $schregNo;
                            $shidou["DATA_ROW"] = $shidouCnt;
                            
                            foreach($sVal as $tKey => $tVal){
                                if(array_key_exists($tKey, $shidouChange)){
                                    $shidou[$shidouChange[$tKey]] = $tVal;
                                }else if($tKey == "自立活動の記録"){
                                    foreach($tVal as $rKey => $rVal){
                                        $shidou[$shidouChange[$rKey]] = $rVal;
                                    }
                                }else if($tKey == "総合的所見及び指導上参考となる諸事項"){
                                    foreach($tVal as $rKey => $rVal){
                                        foreach($rVal as $vKey => $vVal){
                                            $shidou[$shidouChange[$vKey]] = $vVal;
                                        }
                                    }
                                }else if($tKey == "出欠の記録"){
                                    foreach($tVal as $rKey => $rVal){
                                        $shidou[$shidouChange[$rKey]] = $rVal;
                                    }
                                }else if($tKey == "各教科の学習の記録"){
                                    foreach($tVal as $rKey => $rVal){
                                        if($rKey == "観点別学習状況"){
                                            $pointChange = array("必修選択"     => "MUST_CHOICE",
                                                                 "教科"         => "SUBJECT",
                                                                 "教科_その他"  => "OTHER_SUBJECT",
                                                                 "観点"         => "POINT",
                                                                 "観点_その他"  => "OTHER_POINT",
                                                                 "評価"         => "SCORE");
                                                                 
                                            $subjPointCnt = 1;
                                            $subjPoint = array();
                                            $subjPoint["REGISTERCD"] = STAFFCD;
                                            $subjPoint["UPDATED"] = "sysdate()";
                                            $subjPoint["SCHREGNO"] = $schregNo;
                                            $subjPoint["UPPER_DATA_ROW"] = $shidouCnt;
                                            
                                            foreach($rVal as $vKey => $vVal){
                                                if(is_numeric($vKey)){
                                                    $subjPointUpdate = 0;
                                                    
                                                    $subjPoint["REGISTERCD"] = STAFFCD;
                                                    $subjPoint["UPDATED"] = "sysdate()";
                                                    $subjPoint["SCHREGNO"] = $schregNo;
                                                    $subjPoint["UPPER_DATA_ROW"] = $shidouCnt;
                                                    $subjPoint["DATA_ROW"] = $subjPointCnt;
                                                    foreach($vVal as $xKey => $xVal){
                                                        $subjPoint[$pointChange[$xKey]] = $xVal;
                                                    }
                                                    _update(CE_APP_Y_SUBJ_POINT_DAT, "", "", $subjPoint, $db, "2");
                                                    //クリア
                                                    $subjPoint = array();
                                                    $subjPointCnt++;
                                                    
                                                }else{
                                                    $subjPointUpdate = 1;
                                                    
                                                    $subjPoint[$pointChange[$vKey]] = $vVal;
                                                }
                                            }
                                            if($subjPointUpdate != 0){
                                                $subjPoint["DATA_ROW"] = $subjPointCnt;
                                                _update(CE_APP_Y_SUBJ_POINT_DAT, "", "", $subjPoint, $db, "2");
                                            }
                                        }else{  //評定情報
                                            $scoreChange = array("必修選択"     => "MUST_CHOICE",
                                                                 "教科"         => "SUBJECT",
                                                                 "教科_その他"  => "OTHER_SUBJECT",
                                                                 "評定"         => "SCORE");
                                            
                                            $scoreCnt = 1;
                                            $subjScore = array();
                                            $subjScore["REGISTERCD"] = STAFFCD;
                                            $subjScore["UPDATED"] = "sysdate()";
                                            $subjScore["SCHREGNO"] = $schregNo;
                                            $subjScore["UPPER_DATA_ROW"] = $shidouCnt;
                                            
                                            foreach($rVal as $vKey => $vVal){
                                                if(is_numeric($vKey)){
                                                    $scoreUpdate = 0;
                                                    
                                                    $subjScore["REGISTERCD"] = STAFFCD;
                                                    $subjScore["UPDATED"] = "sysdate()";
                                                    $subjScore["SCHREGNO"] = $schregNo;
                                                    $subjScore["UPPER_DATA_ROW"] = $shidouCnt;
                                                    $subjScore["DATA_ROW"] = $scoreCnt;
                                                    
                                                    foreach($vVal as $xKey => $xVal){
                                                        $subjScore[$scoreChange[$xKey]] = $xVal;
                                                    }
                                                    _update(CE_APP_Y_SUBJ_SCORE_DAT, "", "", $subjScore, $db, "2");
                                                    //クリア
                                                    $subjScore = array();
                                                    
                                                    $scoreCnt++;
                                                    
                                                }else{
                                                    $scoreUpdate = 1;
                                                    
                                                    $subjScore[$scoreChange[$vKey]] = $vVal;
                                                }
                                            }
                                            if($scoreUpdate != 0){
                                                $subjScore["DATA_ROW"] = $scoreCnt;
                                                _update(CE_APP_Y_SUBJ_SCORE_DAT, "", "", $subjScore, $db, "2");
                                            }
                                        }
                                    }
                                    //"特別の教科_道徳"追加
                                }else if($tKey == "特別の教科_道徳"){
                                    $forCnt = 1;
                                    $moralChange = array("評価"         =>"POINT");
                                    $moralScore = array();
                                    $moralScore["REGISTERCD"] = STAFFCD;
                                    $moralScore["SCHREGNO"] = $schregNo;
                                    $moralScore["UPPER_DATA_ROW"] = $shidouCnt;
                                    $moralScore["DATA_ROW"] = $forCnt;
                                    $moralScore["UPDATED"] = "sysdate()";
                                    foreach($tVal as $rKey => $rVal){
                                        $moralScore[$moralChange[$rKey]] = $rVal;
                                    }
                                    _update(CE_APP_Y_MORAL_DAT, "", "", $moralScore, $db, "2");
                                
                                }else if($tKey == "外国語活動の記録"){
                                        
                                    foreach($tVal as $rKey => $rVal){
                                        if($rKey == "学習活動の状況"){
                                            $forChange = array("観点"           => "POINT",
                                                                "観点_その他"    => "OTHER_POINT",
                                                                "評価"           => "SCORE");
                                                                //"学習活動の状況評価"           => "SCORE");
                                                                
                                            $forCnt = 1;
                                            $forlang = array();
                                            $forlang["REGISTERCD"] = STAFFCD;
                                            $forlang["UPDATED"] = "sysdate()";
                                            $forlang["SCHREGNO"] = $schregNo;
                                            $forlang["UPPER_DATA_ROW"] = $shidouCnt;
                                            
                                            foreach($rVal as $vKey => $vVal){
                                                if(is_numeric($vKey)){
                                                    $forUpdate = 0;
                                                    
                                                    $forlang["REGISTERCD"] = STAFFCD;
                                                    $forlang["UPDATED"] = "sysdate()";
                                                    $forlang["SCHREGNO"] = $schregNo;
                                                    $forlang["UPPER_DATA_ROW"] = $shidouCnt;
                                                    $forlang["DATA_ROW"] = $forCnt;
                                                    
                                                    foreach($vVal as $xKey => $xVal){
                                                        $forlang[$forChange[$xKey]] = $xVal;
                                                    }
                                                    _update(CE_APP_Y_FOR_LANG_DAT, "", "", $forlang, $db, "2");
                                                    //クリア
                                                    $forlang = array();
                                                    
                                                    $forCnt++;
                                                    
                                                }else{
                                                    $forUpdate = 1;
                                                    $forlang[$forChange[$vKey]] = $vVal;
                                                }
                                            }
                                            if($forUpdate != 0){
                                                $forlang["DATA_ROW"] = $forCnt;
                                                _update(CE_APP_Y_FOR_LANG_DAT, "", "", $forlang, $db, "2");
                                            }

                                        }else if($rKey == "評価"){
                                            $forChange = array("評価"           => "ONLY_SCORE");
                                            $forCnt = 1;
                                            $forlang = array();
                                            $forlang["REGISTERCD"] = STAFFCD;
                                            $forlang["UPDATED"] = "sysdate()";
                                            $forlang["SCHREGNO"] = $schregNo;
                                            $forlang["UPPER_DATA_ROW"] = $shidouCnt;
                                            $forlang["DATA_ROW"] = $forCnt;
                                            $forlang[$forChange[$rKey]] = $rVal;                                                       
                                            _update(CE_APP_Y_FOR_LANG_DAT, "", "", $forlang, $db, "2");
                                            //クリア
                                            $forlang = array();
                                        }
                                    }
                                }else if($tKey == "総合的な学習の時間の記録"){
                                    $integChange = array("学習活動" => "ACTION",
                                                         "評価"     => "SCORE");
                                                         
                                    $integCnt = 1;
                                    $integ = array();
                                    $integ["REGISTERCD"] = STAFFCD;
                                    $integ["UPDATED"] = "sysdate()";
                                    $integ["SCHREGNO"] = $schregNo;
                                    $integ["UPPER_DATA_ROW"] = $shidouCnt;
                                    
                                    $integPointCnt = 1;
                                    $integPoint = array();
                                    $integPoint["REGISTERCD"] = STAFFCD;
                                    $integPoint["UPDATED"] = "sysdate()";
                                    $integPoint["SCHREGNO"] = $schregNo;
                                    $integPoint["UPPER_DATA_ROW"] = $shidouCnt;
                                    
                                    foreach($tVal as $rKey => $rVal){
                                        foreach($rVal as $vKey => $vVal){
                                            if(is_numeric($vKey)){
                                                $integUpdate = 0;
                                                
                                                $integ["DATA_ROW"] = $integCnt;
                                                $integPoint["NEXT_DATA_ROW"] = $integCnt;
                                                foreach($vVal as $xKey => $xVal){
                                                    if(array_key_exists($xKey, $integChange)){
                                                        $integ[$integChange[$xKey]] = $xVal;
                                                    }else{  //観点
                                                        if(is_array($xVal) && !empty($xVal)){
                                                            foreach($xVal as $nKey => $nVal){
                                                                $integPoint["DATA_ROW"] = $integPointCnt;
                                                                
                                                                $integPoint["POINT"] = $nVal;
                                                                
                                                                _update(CE_APP_Y_INTEG_STUDY_POINT_DAT, "", "", $integPoint, $db, "2");
                                                                $integPointCnt++;
                                                            }
                                                        }else{
                                                            $integPoint["DATA_ROW"] = $integPointCnt;
                                                            $integPoint["POINT"] = $xVal;
                                                            _update(CE_APP_Y_INTEG_STUDY_POINT_DAT, "", "", $integPoint, $db, "2");
                                                        }
                                                    }
                                                }
                                                _update(CE_APP_Y_INTEG_STUDY_DAT, "", "", $integ, $db, "2");
                                                //クリア
                                                $integ["ACTION"] = "";
                                                
                                                $integCnt++;
                                            }else{
                                                $integUpdate = 1;
                                                $integPoint["NEXT_DATA_ROW"] = $integCnt;
                                                
                                                if(array_key_exists($vKey, $integChange)){
                                                    $integ[$integChange[$vKey]] = $vVal;
                                                }else{
                                                    if(is_array($vVal) && !empty($vVal)){
                                                        foreach($vVal as $xKey => $xVal){
                                                            $integPoint["DATA_ROW"] = $integPointCnt;
                                                            $integPoint["POINT"] = $xVal;
                                                            
                                                            _update(CE_APP_Y_INTEG_STUDY_POINT_DAT, "", "", $integPoint, $db, "2");
                                                            $integPointCnt++;
                                                        }
                                                    }else{
                                                        $integPoint["DATA_ROW"] = $integPointCnt;
                                                        $integPoint["POINT"] = $vVal;
                                                        
                                                        _update(CE_APP_Y_INTEG_STUDY_POINT_DAT, "", "", $integPoint, $db, "2");
                                                    }
                                                }
                                            }
                                        }
                                        if($integUpdate != 0){
                                            $integ["DATA_ROW"] = $integCnt;
                                            _update(CE_APP_Y_INTEG_STUDY_DAT, "", "", $integ, $db, "2");
                                        }
                                    }
                                }else if($tKey == "特別活動の記録"){
                                    $spChange = array("内容"        => "ACTION", 
                                                      "内容_その他" => "OTHER_ACTION",
                                                      "観点"        => "POINT",
                                                      "評価"        => "SCORE");
                                    
                                    $spCnt = 1;
                                    $spAct = array();
                                    $spAct["REGISTERCD"] = STAFFCD;
                                    $spAct["UPDATED"] = "sysdate()";
                                    $spAct["SCHREGNO"] = $schregNo;
                                    $spAct["UPPER_DATA_ROW"] = $shidouCnt;

                                    foreach($tVal as $rKey => $rVal){
                                        foreach($rVal as $vKey => $vVal){
                                            if(is_numeric($vKey)){
                                                $spUpdate = 0;
                                                
                                                $spAct["DATA_ROW"] = $spCnt;
                                                foreach($vVal as $xKey => $xVal){
                                                    $spAct[$spChange[$xKey]] = $xVal;
                                                }
                                                _update(CE_APP_Y_SP_ACT_DAT, "", "", $spAct, $db, "2");
                                                //クリア
                                                $spAct["POINT"] = "";
                                                
                                                $spCnt++;
                                                
                                            }else{
                                                $spUpdate = 1;
                                                
                                                $spAct[$spChange[$vKey]] = $vVal;
                                            }
                                        }
                                        if($spUpdate != 0){
                                            $spAct["DATA_ROW"] = $spCnt;
                                            _update(CE_APP_Y_SP_ACT_DAT, "", "", $spAct, $db, "2");
                                        }
                                    }
                                }else if($tKey == "行動の記録"){
                                    $behavChange = array("項目"         => "ITEM",
                                                         "項目_その他"  => "OTHER_ITEM",
                                                         "評価"         => "SCORE_CODE");
                                    
                                    $behavCnt = 1;
                                    $behav = array();
                                    $behav["REGISTERCD"] = STAFFCD;
                                    $behav["UPDATED"] = "sysdate()";
                                    $behav["SCHREGNO"] = $schregNo;
                                    $behav["UPPER_DATA_ROW"] = $shidouCnt;
                                    
                                    foreach($tVal as $rKey => $rVal){
                                        if($rKey == "行動の状況"){
                                            foreach($rVal as $vKey => $vVal){
                                                if(is_numeric($vKey)){
                                                    $behavUpdate = 0;
                                                    
                                                    $behav["REGISTERCD"] = STAFFCD;
                                                    $behav["UPDATED"] = "sysdate()";
                                                    $behav["SCHREGNO"] = $schregNo;
                                                    $behav["UPPER_DATA_ROW"] = $shidouCnt;
                                                    $behav["DATA_ROW"] = $behavCnt;
                                                    
                                                    foreach($vVal as $xKey => $xVal){
                                                        $behav[$behavChange[$xKey]] = $xVal;
                                                    }
                                                    _update(CE_APP_Y_BEHAVIOR_DAT, "", "", $behav, $db, "2");
                                                    //クリア
                                                    $behav = array();
                                                    
                                                    $behavCnt++;
                                                    
                                                }else{
                                                    $behavUpdate = 1;
                                                    
                                                    $behav[$behavChange[$vKey]] = $vVal;
                                                }
                                            }
                                            if($behavUpdate != 0){
                                                $behav["DATA_ROW"] = $behavCnt;
                                                _update(CE_APP_Y_BEHAVIOR_DAT, "", "", $behav, $db, "2");
                                            }
                                        }else{  //評価
                                            $behav["DATA_ROW"] = $behavCnt;
                                            $behav["SCORE"] = $rVal;
                                            _update(CE_APP_Y_BEHAVIOR_DAT, "", "", $behav, $db, "2");
                                            //一応クリア(次が行動の状況だったときのため)
                                            $behav["SCORE"] = "";
                                        }
                                    }
                                }else if($tKey == "各教科_特別活動_自立活動の記録"){
                                    $allChange = array("教科等"         => "SUBJECT",
                                                       "教科等_その他"  => "OTHER_SUBJECT",
                                                       "評価"           => "SCORE");
                                    
                                    $allCnt = 1;
                                    $all = array();
                                    $all["REGISTERCD"] = STAFFCD;
                                    $all["UPDATED"] = "sysdate()";
                                    $all["SCHREGNO"] = $schregNo;
                                    $all["UPPER_DATA_ROW"] = $shidouCnt;
                                    
                                    foreach($tVal as $rKey => $rVal){
                                        foreach($rVal as $vKey => $vVal){
                                            if(is_numeric($vKey)){
                                                $allUpdate = 0;
                                                
                                                $all["REGISTERCD"] = STAFFCD;
                                                $all["UPDATED"] = "sysdate()";
                                                $all["SCHREGNO"] = $schregNo;
                                                $all["UPPER_DATA_ROW"] = $shidouCnt;
                                                
                                                $all["DATA_ROW"] = $allCnt;
                                                
                                                foreach($vVal as $xKey => $xVal){
                                                    $all[$allChange[$xKey]] = $xVal;
                                                }
                                                _update(CE_APP_Y_ALL_ACT_DAT, "", "", $all, $db, "2");
                                                //クリア
                                                $all = array();
                                                $allCnt++;
                                                
                                            }else{
                                                $allUpdate = 1;
                                                
                                                $all[$allChange[$vKey]] = $vVal;
                                            }
                                        }
                                        if($allUpdate != 0){
                                            $all["DATA_ROW"] = $allCnt;
                                            _update(CE_APP_Y_ALL_ACT_DAT, "", "", $all, $db, "2");
                                        }
                                    }
                                }
                            }
                            _update(CE_APP_Y_SHIDOU_DAT, "", "", $shidou, $db, "2");
                            //クリア
                            $shidou = array();
                            
                            $shidouCnt++;
                            
                        }else{
                            $shidouUpdate = 1;
                            
                            if(array_key_exists($sKey, $shidouChange)){
                                $shidou[$shidouChange[$sKey]] = $sVal;
                            }else if($sKey == "自立活動の記録"){
                                foreach($sVal as $tKey => $tVal){
                                    $shidou[$shidouChange[$tKey]] = $tVal;
                                }
                            }else if($sKey == "総合的所見及び指導上参考となる諸事項"){
                                foreach($sVal as $tKey => $tVal){
                                    foreach($tVal as $rKey => $rVal){
                                        $shidou[$shidouChange[$rKey]] = $rVal;
                                    }
                                }
                            }else if($sKey == "出欠の記録"){
                                foreach($sVal as $tKey => $tVal){
                                    $shidou[$shidouChange[$tKey]] = $tVal;
                                }
                            }else if($sKey == "各教科の学習の記録"){
                                foreach($sVal as $tKey => $tVal){
                                    if($tKey == "観点別学習状況"){
                                        $pointChange = array("必修選択"     => "MUST_CHOICE",
                                                             "教科"         => "SUBJECT",
                                                             "教科_その他"  => "OTHER_SUBJECT",
                                                             "観点"         => "POINT",
                                                             "観点_その他"  => "OTHER_POINT",
                                                             "評価"         => "SCORE");
                                                             
                                        $subjPointCnt = 1;
                                        $subjPoint = array();
                                        $subjPoint["REGISTERCD"] = STAFFCD;
                                        $subjPoint["UPDATED"] = "sysdate()";
                                        $subjPoint["SCHREGNO"] = $schregNo;
                                        $subjPoint["UPPER_DATA_ROW"] = $shidouCnt;
                                        
                                        foreach($tVal as $rKey => $rVal){
                                            if(is_numeric($rKey)){
                                                $subjPointUpdate = 0;
                                                
                                                $subjPoint["REGISTERCD"] = STAFFCD;
                                                $subjPoint["UPDATED"] = "sysdate()";
                                                $subjPoint["SCHREGNO"] = $schregNo;
                                                $subjPoint["UPPER_DATA_ROW"] = $shidouCnt;
                                                $subjPoint["DATA_ROW"] = $subjPointCnt;
                                                foreach($rVal as $vKey => $vVal){
                                                    $subjPoint[$pointChange[$vKey]] = $vVal;
                                                }
                                                _update(CE_APP_Y_SUBJ_POINT_DAT, "", "", $subjPoint, $db, "2");
                                                //クリア
                                                $subjPoint = array();
                                                $subjPointCnt++;
                                                
                                            }else{
                                                $subjPointUpdate = 1;
                                                
                                                $subjPoint[$pointChange[$rKey]] = $rVal;
                                            }
                                        }
                                        if($subjPointUpdate != 0){
                                            $subjPoint["DATA_ROW"] = $subjPointCnt;
                                            _update(CE_APP_Y_SUBJ_POINT_DAT, "", "", $subjPoint, $db, "2");
                                        }
                                    }else{  //評定情報
                                        $scoreChange = array("必修選択"     => "MUST_CHOICE",
                                                             "教科"         => "SUBJECT",
                                                             "教科_その他"  => "OTHER_SUBJECT",
                                                             "評定"         => "SCORE");
                                        
                                        $scoreCnt = 1;
                                        $subjScore = array();
                                        $subjScore["REGISTERCD"] = STAFFCD;
                                        $subjScore["UPDATED"] = "sysdate()";
                                        $subjScore["SCHREGNO"] = $schregNo;
                                        $subjScore["UPPER_DATA_ROW"] = $shidouCnt;
                                        
                                        foreach($tVal as $rKey => $rVal){
                                            if(is_numeric($rKey)){
                                                $scoreUpdate = 0;
                                                
                                                $subjScore["REGISTERCD"] = STAFFCD;
                                                $subjScore["UPDATED"] = "sysdate()";
                                                $subjScore["SCHREGNO"] = $schregNo;
                                                $subjScore["UPPER_DATA_ROW"] = $shidouCnt;
                                                $subjScore["DATA_ROW"] = $scoreCnt;
                                                
                                                foreach($rVal as $vKey => $vVal){
                                                    $subjScore[$scoreChange[$vKey]] = $vVal;
                                                }
                                                _update(CE_APP_Y_SUBJ_SCORE_DAT, "", "", $subjScore, $db, "2");
                                                //クリア
                                                $subjScore = array();
                                                
                                                $scoreCnt++;
                                                
                                            }else{
                                                $scoreUpdate = 1;
                                                
                                                $subjScore[$scoreChange[$rKey]] = $rVal;
                                            }
                                        }
                                        if($scoreUpdate != 0){
                                            $subjScore["DATA_ROW"] = $scoreCnt;
                                            _update(CE_APP_Y_SUBJ_SCORE_DAT, "", "", $subjScore, $db, "2");
                                        }
                                    }
                                }
                            //"特別の教科_道徳"追加
                            }else if($sKey == "特別の教科_道徳"){
                                $moralChange = array("評価"         =>"POINT");
                                $moralScore = array();
                                $forCnt = 1;
                                $moralScore["REGISTERCD"] = STAFFCD;
                                $moralScore["SCHREGNO"] = $schregNo;
                                $moralScore["UPPER_DATA_ROW"] = $shidouCnt;
                                $moralScore["DATA_ROW"] = $forCnt;
                                $moralScore["UPDATED"] = "sysdate()";
                                foreach($sVal as $tKey => $tVal){
                                        $moralScore[$moralChange[$tKey]] = $tVal;
                                }
                                _update(CE_APP_Y_MORAL_DAT, "", "", $moralScore, $db, "2");
                            
                            }else if($sKey == "外国語活動の記録"){

                                foreach($sVal as $tKey => $tVal){
                                    if($tKey == "学習活動の状況"){
                                        $forChange = array("観点"           => "POINT",
                                        "観点_その他"    => "OTHER_POINT",
                                        "評価"           => "SCORE");
                                        //"学習活動の状況評価"           => "SCORE"
                                        
                     $forCnt = 1;
                     $forlang = array();
                     $forlang["REGISTERCD"] = STAFFCD;
                     $forlang["UPDATED"] = "sysdate()";
                     $forlang["SCHREGNO"] = $schregNo;
                     $forlang["UPPER_DATA_ROW"] = $shidouCnt;
                                    
                                    foreach($tVal as $rKey => $rVal){
                                        if(is_numeric($rKey)){
                                            $forUpdate = 0;
                                            
                                            $forlang["REGISTERCD"] = STAFFCD;
                                            $forlang["UPDATED"] = "sysdate()";
                                            $forlang["SCHREGNO"] = $schregNo;
                                            $forlang["UPPER_DATA_ROW"] = $shidouCnt;
                                            $forlang["DATA_ROW"] = $forCnt;
                                            
                                            foreach($rVal as $vKey => $vVal){
                                                $forlang[$forChange[$vKey]] = $vVal;
                                            }
                                            _update(CE_APP_Y_FOR_LANG_DAT, "", "", $forlang, $db, "2");
                                            //クリア
                                            $forlang = array();
                                            
                                            $forCnt++;
                                            
                                        }else{
                                            $forUpdate = 1;
                                            
                                            $forlang[$forChange[$rKey]] = $rVal;
                                        }
                                    }
                                    if($forUpdate != 0){
                                        $forlang["DATA_ROW"] = $forCnt;
                                        _update(CE_APP_Y_FOR_LANG_DAT, "", "", $forlang, $db, "2");
                                    }
                                }else if($tKey == "評価"){
                                    $forChange = array("評価"           => "ONLY_SCORE");
                                    $forCnt = 1;
                                    $forlang = array();
                                    $forlang["REGISTERCD"] = STAFFCD;
                                    $forlang["UPDATED"] = "sysdate()";
                                    $forlang["SCHREGNO"] = $schregNo;
                                    $forlang["UPPER_DATA_ROW"] = $shidouCnt;
                                    $forlang["DATA_ROW"] = $forCnt;
                                    $forlang[$forChange[$tKey]] = $tVal;                                                       
                                    _update(CE_APP_Y_FOR_LANG_DAT, "", "", $forlang, $db, "2");
                                    //クリア
                                    $forlang = array();
                                }
                                }
                            }else if($sKey == "総合的な学習の時間の記録"){
                                $integChange = array("学習活動" => "ACTION",
                                                     "評価"     => "SCORE");
                                                     
                                $integCnt = 1;
                                $integ = array();
                                $integ["REGISTERCD"] = STAFFCD;
                                $integ["UPDATED"] = "sysdate()";
                                $integ["SCHREGNO"] = $schregNo;
                                $integ["UPPER_DATA_ROW"] = $shidouCnt;
                                
                                $integPointCnt = 1;
                                $integPoint = array();
                                $integPoint["REGISTERCD"] = STAFFCD;
                                $integPoint["UPDATED"] = "sysdate()";
                                $integPoint["SCHREGNO"] = $schregNo;
                                $integPoint["UPPER_DATA_ROW"] = $shidouCnt;
                                
                                foreach($sVal as $tKey => $tVal){
                                    foreach($tVal as $rKey => $rVal){
                                        if(is_numeric($rKey)){
                                            $integUpdate = 0;
                                            
                                            $integ["DATA_ROW"] = $integCnt;
                                            $integPoint["NEXT_DATA_ROW"] = $integCnt;
                                            foreach($rVal as $vKey => $vVal){
                                                if(array_key_exists($vKey, $integChange)){
                                                    $integ[$integChange[$vKey]] = $vVal;
                                                }else{  //観点
                                                    if(is_array($vVal) && !empty($vVal)){
                                                        foreach($vVal as $xKey => $xVal){
                                                            $integPoint["DATA_ROW"] = $integPointCnt;
                                                            
                                                            $integPoint["POINT"] = $xVal;
                                                            
                                                            _update(CE_APP_Y_INTEG_STUDY_POINT_DAT, "", "", $integPoint, $db, "2");
                                                            $integPointCnt++;
                                                        }
                                                    }else{
                                                        $integPoint["DATA_ROW"] = $integPointCnt;
                                                        $integPoint["POINT"] = $vVal;
                                                        _update(CE_APP_Y_INTEG_STUDY_POINT_DAT, "", "", $integPoint, $db, "2");
                                                    }
                                                }
                                            }
                                            _update(CE_APP_Y_INTEG_STUDY_DAT, "", "", $integ, $db, "2");
                                            //クリア
                                            $integ["ACTION"] = "";
                                            
                                            $integCnt++;
                                        }else{
                                            $integUpdate = 1;
                                            $integPoint["NEXT_DATA_ROW"] = $integCnt;
                                            
                                            if(array_key_exists($rKey, $integChange)){
                                                $integ[$integChange[$rKey]] = $rVal;
                                            }else{
                                                if(is_array($rVal) && !empty($rVal)){
                                                    foreach($rVal as $vKey => $vVal){
                                                        $integPoint["DATA_ROW"] = $integPointCnt;
                                                        $integPoint["POINT"] = $vVal;
                                                        
                                                        _update(CE_APP_Y_INTEG_STUDY_POINT_DAT, "", "", $integPoint, $db, "2");
                                                        $integPointCnt++;
                                                    }
                                                }else{
                                                    $integPoint["DATA_ROW"] = $integPointCnt;
                                                    $integPoint["POINT"] = $rVal;
                                                    
                                                    _update(CE_APP_Y_INTEG_STUDY_POINT_DAT, "", "", $integPoint, $db, "2");
                                                }
                                            }
                                        }
                                    }
                                    if($integUpdate != 0){
                                        $integ["DATA_ROW"] = $integCnt;
                                        _update(CE_APP_Y_INTEG_STUDY_DAT, "", "", $integ, $db, "2");
                                    }
                                }
                            }else if($sKey == "特別活動の記録"){
                                $spChange = array("内容"        => "ACTION", 
                                                  "内容_その他" => "OTHER_ACTION",
                                                  "観点"        => "POINT",
                                                  "評価"        => "SCORE");
                                
                                $spCnt = 1;
                                $spAct = array();
                                $spAct["REGISTERCD"] = STAFFCD;
                                $spAct["UPDATED"] = "sysdate()";
                                $spAct["SCHREGNO"] = $schregNo;
                                $spAct["UPPER_DATA_ROW"] = $shidouCnt;

                                foreach($sVal as $tKey => $tVal){
                                    foreach($tVal as $rKey => $rVal){
                                        if(is_numeric($rKey)){
                                            $spUpdate = 0;
                                            
                                            $spAct["DATA_ROW"] = $spCnt;
                                            foreach($rVal as $vKey => $vVal){
                                                $spAct[$spChange[$vKey]] = $vVal;
                                            }
                                            _update(CE_APP_Y_SP_ACT_DAT, "", "", $spAct, $db, "2");
                                            //クリア
                                            $spAct["POINT"] = "";
                                            
                                            $spCnt++;
                                            
                                        }else{
                                            $spUpdate = 1;
                                            
                                            $spAct[$spChange[$rKey]] = $rVal;
                                        }
                                    }
                                    if($spUpdate != 0){
                                        $spAct["DATA_ROW"] = $spCnt;
                                        _update(CE_APP_Y_SP_ACT_DAT, "", "", $spAct, $db, "2");
                                    }
                                }
                            }else if($sKey == "行動の記録"){
                                $behavChange = array("項目"         => "ITEM",
                                                     "項目_その他"  => "OTHER_ITEM",
                                                     "評価"         => "SCORE_CODE");
                                
                                $behavCnt = 1;
                                $behav = array();
                                $behav["REGISTERCD"] = STAFFCD;
                                $behav["UPDATED"] = "sysdate()";
                                $behav["SCHREGNO"] = $schregNo;
                                $behav["UPPER_DATA_ROW"] = $shidouCnt;
                                
                                foreach($sVal as $tKey => $tVal){
                                    if($tKey == "行動の状況"){
                                        foreach($tVal as $rKey => $rVal){
                                            if(is_numeric($rKey)){
                                                $behavUpdate = 0;
                                                
                                                $behav["REGISTERCD"] = STAFFCD;
                                                $behav["UPDATED"] = "sysdate()";
                                                $behav["SCHREGNO"] = $schregNo;
                                                $behav["UPPER_DATA_ROW"] = $shidouCnt;
                                                $behav["DATA_ROW"] = $behavCnt;
                                                
                                                foreach($rVal as $vKey => $vVal){
                                                    $behav[$behavChange[$vKey]] = $vVal;
                                                }
                                                _update(CE_APP_Y_BEHAVIOR_DAT, "", "", $behav, $db, "2");
                                                //クリア
                                                $behav = array();
                                                
                                                $behavCnt++;
                                                
                                            }else{
                                                $behavUpdate = 1;
                                                
                                                $behav[$behavChange[$rKey]] = $rVal;
                                            }
                                        }
                                        if($behavUpdate != 0){
                                            $behav["DATA_ROW"] = $behavCnt;
                                            _update(CE_APP_Y_BEHAVIOR_DAT, "", "", $behav, $db, "2");
                                        }
                                    }else{  //評価
                                        $behav["DATA_ROW"] = $behavCnt;
                                        $behav["SCORE"] = $tVal;
                                        _update(CE_APP_Y_BEHAVIOR_DAT, "", "", $behav, $db, "2");
                                        //一応クリア(次が行動の状況だったときのため)
                                        $behav["SCORE"] = "";
                                    }
                                }
                            }else if($sKey == "各教科_特別活動_自立活動の記録"){
                                $allChange = array("教科等"         => "SUBJECT",
                                                   "教科等_その他"  => "OTHER_SUBJECT",
                                                   "評価"           => "SCORE");
                                
                                $allCnt = 1;
                                $all = array();
                                $all["REGISTERCD"] = STAFFCD;
                                $all["UPDATED"] = "sysdate()";
                                $all["SCHREGNO"] = $schregNo;
                                $all["UPPER_DATA_ROW"] = $shidouCnt;
                                
                                foreach($sVal as $tKey => $tVal){
                                    foreach($tVal as $rKey => $rVal){
                                        if(is_numeric($rKey)){
                                            $allUpdate = 0;
                                            
                                            $all["REGISTERCD"] = STAFFCD;
                                            $all["UPDATED"] = "sysdate()";
                                            $all["SCHREGNO"] = $schregNo;
                                            $all["UPPER_DATA_ROW"] = $shidouCnt;
                                            
                                            $all["DATA_ROW"] = $allCnt;
                                            
                                            foreach($rVal as $vKey => $vVal){
                                                $all[$allChange[$vKey]] = $vVal;
                                            }
                                            _update(CE_APP_Y_ALL_ACT_DAT, "", "", $all, $db, "2");
                                            //クリア
                                            $all = array();
                                            $allCnt++;
                                            
                                        }else{
                                            $allUpdate = 1;
                                            
                                            $all[$allChange[$rKey]] = $rVal;
                                        }
                                    }
                                    if($allUpdate != 0){
                                        $all["DATA_ROW"] = $allCnt;
                                        _update(CE_APP_Y_ALL_ACT_DAT, "", "", $all, $db, "2");
                                    }
                                }
                            }
                        }
                    }
                    if($shidouUpdate != 0){
                        $shidou["DATA_ROW"] = $shidouCnt;
                        _update(CE_APP_Y_SHIDOU_DAT, "", "", $shidou, $db, "2");
                    }
                }else if($fKey == "入学時の障害の状態"){
                    $idou["HANDICAP"] = $fVal["障害の状態"];
                }
            }
        }
    }
    
    //CE_APP_YOUROKU_テーブル更新
    _update(CE_APP_YOUROKU_TO_MST, "", "", $yourokuTo, $db, "2");
    _update(CE_APP_YOUROKU_FROM_MST, "", "", $yourokuFrom, $db, "2");
    _update(CE_APP_YOUROKU_SYUBETU_MST, "", "", $syubetu, $db, "2");
    _update(CE_APP_YOUROKU_IDOU_MST, "", "", $idou, $db, "2");
    
    
    //DB切断
    Query::dbCheckIn($db);
    
    //$this->setMessage("更新しました。");
    
    
    /*print_r($yourokuTo);
    echo "<BR><BR>";
    print_r($yourokuFrom);
    echo "<BR><BR>";
    print_r($syubetu);
    echo "<BR><BR>";
    print_r($idou);
    echo "<BR><BR>";*/
}

//学齢簿情報取り込み
function ceGakureiboUpdate($schregNo, $data) 
{
    //更新用
    $gakureiF =array();
    $gakureiS = array();
    
    $gakureiF["REGISTERCD"] = STAFFCD;
    $gakureiF["UPDATED"] = "sysdate()";
    $gakureiF["SCHREGNO"] = $schregNo;
    
    $gakureiS["REGISTERCD"] = STAFFCD;
    $gakureiS["UPDATED"] = "sysdate()";
    $gakureiS["SCHREGNO"] = $schregNo;
    
    $name = array("外字氏名" => "GNAME",
                  "内字氏名" => "NNAME",
                  "フリガナ" => "NAME_KANA");
    
    $addr = array("住所コード" => "ADDRCD",
                  "外字住所"   => "GADDR",
                  "内字住所"   => "NADDR",
                  "外字方書"   => "GKATAGAKI",
                  "内字方書"   => "NKATAGAKI",
                  "郵便番号"   => "ZIPCD");
    
    $date = array("年" => "YEAR",
                  "月" => "MONTH",
                  "日" => "DAY");
                  
    
    //DB接続
    $db = Query::dbCheckOut();
    //中学校情報のフラグ
    $jFlg = 0;
    foreach($data as $gKey => $gVal){
        if($gKey == "利用業務ユニット"){
            
            $gakureiF["GYOUMU_UNIT"] = $gVal;
            
        }else if($gKey == "送信日時"){
            
            //Tを半角スペースに変換
            $send = str_replace("T", " ", $gVal);
            $send = explode("+", $send);
            $send_date = date("Y-m-d H:i:s", strtotime($send[0]));

            // preg_match('/(?<=\.)[0-9]*(?=\+)/', $send, $send_msc);
            
            // if(mb_strlen($send_msc[0]) > 6 ){
            //     $send_date = preg_replace('/^.*(?=[0-9]\+)/', "" ,$send);
            // } else {
            //     //+から上だけ使う
            //     $send_date = explode("+", $send);
            // }

            //$gakureiS["SEND_DATE"] = "'".$send_date[0].".000000'";
            $gakureiS["SEND_DATE"] = "'$send_date'";

        }else if($gKey == "学齢簿情報"){
            foreach($gVal as $fKey => $fVal){
                if($fKey == "異動変更区分"){
                    
                    $gakureiF["CHANGE_KUBUN"] = $fVal;
                    
                }else if($fKey == "異動変更年月日"){
                    $top = "CHANGE_";
                    foreach($fVal as $sKey => $sVal){
                        $gakureiF[$top.$date[$sKey]] = $sVal;
                    }
                    
                }else if($fKey == "児童生徒情報"){
                    $student = array("識別番号" => "S_SHIKIBETU_NO",
                                     "世帯番号" => "S_SETAI_NO",
                                     "性別"     => "S_SEX",
                                     "年号"     => "S_BIRTH_ERA",
                                     "学年"     => "S_GRADE",
                                     "小学校区" => "S_P_AREA",
                                     "中学校区" => "S_J_AREA",
                                     "援助区分" => "ASSIST_KUBUN",
                                     "学級区分" => "CLASS_KUBUN");
                                     
                    foreach($fVal as $sKey => $sVal){
                        if(array_key_exists($sKey, $student)){
                            $gakureiF[$student[$sKey]] = $sVal;
                        }else{
                            $top = "S_";
                            if($sKey == "氏名"){
                                foreach($sVal as $tKey => $tVal){
                                    $gakureiF[$top.$name[$tKey]] = $tVal;
                                }
                            }else if($sKey == "住所"){
                                foreach($sVal as $tKey => $tVal){
                                    $gakureiF[$top.$addr[$tKey]] = $tVal;
                                }
                            }else if($sKey == "生年月日"){
                                foreach($sVal as $tKey => $tVal){
                                    if(array_key_exists($tKey, $student)){
                                        $gakureiF[$student[$tKey]] = $tVal;
                                    }else{
                                        $mid = "BIRTH_";
                                        foreach($tVal as $rKey => $rVal){
                                            $gakureiF[$top.$mid.$date[$rKey]] = $rVal;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }else if($fKey == "保護者情報"){
                    $parent = array("識別番号"  => "P_SHIKIBETU_NO",
                                    "電話番号"  => "P_TELNO",
                                    "続柄1"     => "P_RELATION1",
                                    "続柄2"     => "P_RELATION2",
                                    "続柄3"     => "P_RELATION3",
                                    "続柄4"     => "P_RELATION4");
                    
                    foreach($fVal as $sKey => $sVal){
                        if(array_key_exists($sKey, $parent)){
                            $gakureiF[$parent[$sKey]] = $sVal;
                        }else{
                            $top = "P_";
                            if($sKey == "氏名"){
                                foreach($sVal as $tKey => $tVal){
                                    $gakureiF[$top.$name[$tKey]] = $tVal;
                                }
                            }else if($sKey == "住所"){
                                foreach($sVal as $tKey => $tVal){
                                    $gakureiF[$top.$addr[$tKey]] = $tVal;
                                }
                            }else if($sKey == "児童生徒との続柄"){
                                foreach($sVal as $tKey => $tVal){
                                    $gakureiF[$parent[$tKey]] = $tVal;
                                }
                            }
                        }
                    }
                }else if($fKey == "身元引受情報"){
                    $mimoto = array("識別番号"          => "G_SHIKIBETU_NO",
                                    "電話番号"          => "G_TELNO",
                                    "児童生徒との関係"  => "G_RELATION");
                                    
                    foreach($fVal as $sKey => $sVal){
                        if(array_key_exists($sKey, $mimoto)){
                            $gakureiS[$mimoto[$sKey]] = $sVal;
                        }else{
                            $top = "G_";
                            if($sKey == "氏名"){
                                foreach($sVal as $tKey => $tVal){
                                    $gakureiS[$top.$name[$tKey]] = $tVal;
                                }
                            }else if($sKey == "住所"){
                                foreach($sVal as $tKey => $tVal){
                                    $gakureiS[$top.$addr[$tKey]] = $tVal;
                                }
                            }
                        }
                    }
                }else if($fKey == "小学校情報" || $fKey == "中学校情報"){
                    $school = array("学校区分" => "KUBUN",
                                    "学校名"   => "NAME");
                    
                    $simple = array("入学年月日"    => "ENT_", 
                                    "転入学年月日"  => "TRANSE_", 
                                    "編入学年月日"  => "ADM_", 
                                    "卒業年月日"    => "GRD_", 
                                    "転学年月日"    => "SWITCH_", 
                                    "退学年月日"    => "QUIT_");
                                     
                    if($fKey == "小学校情報"){
                        $top = "PRI_";
                    }else{
                        $top = "JNR_";
                        $jFlg = 1;
                    }
                    
                    //複数テーブル用
                    $upper = "CE_APP_G_".$top;
                    $mltTable = array("区域外就学情報"      => "EXTRA_DAT",
                                      "指定外就学情報"      => "OUTSIDE_DAT",
                                      "就学の督促情報"      => "DEMAND_DAT",
                                      "就学の猶予免除情報"  => "POST_DAT");
                    
                    $mlt = array("理由"     => "REASON",
                                 "督促内容" => "DETAIL");
                                 
                    
                    foreach($fVal as $sKey => $sVal){
                        if(array_key_exists($sKey, $school)){
                            $gakureiS[$top.$school[$sKey]] = $sVal;
                        }else if(array_key_exists($sKey, $simple)){
                            $first = $top.$simple[$sKey];
                            foreach($sVal as $tKey => $tVal){
                                $gakureiS[$first.$date[$tKey]] = $tVal;
                            }
                            
                        }else{
                            //複数有
                            $updateTable = $upper.$mltTable[$sKey];
                            $cnt = 1;
                            $mltUpdate = array();
                            $mltUpdate["REGISTERCD"] = STAFFCD;
                            $mltUpdate["UPDATED"] = "sysdate()";
                            $mltUpdate["SCHREGNO"] = $schregNo;
                            
                            foreach($sVal as $tKey => $tVal){
                                
                                if(is_numeric($tKey)){
                                    $mltUpdateFlg = 0;
                                    
                                    $mltUpdate["DATA_ROW"] = $cnt;
                                    
                                    foreach($tVal as $rKey => $rVal){
                                        if(array_key_exists($rKey, $mlt)){
                                            $mltUpdate[$mlt[$rKey]] = $rVal;
                                        }else{
                                            if($rKey == "開始年月日"){
                                                $set = "ST_";
                                            }else if($rKey == "終了年月日"){
                                                $set = "ED_";
                                            }else{
                                                $set = "";
                                            }
                                            foreach($rVal as $vKey => $vVal){
                                                $mltUpdate[$set.$date[$vKey]] = $vVal;
                                            }
                                        }
                                    }
                                    
                                    _update($updateTable, "", "", $mltUpdate, $db, "2");
                                    //クリア
                                    foreach($date as $key => $val){
                                        $mltUpdate["ED_".$val] = "";
                                    }
                                    $cnt++;
                                    
                                }else{
                                    $mltUpdateFlg = 1;
                                    if(array_key_exists($tKey, $mlt)){
                                        $mltUpdate[$mlt[$tKey]] = $tVal;
                                    }else{
                                        if($tKey == "開始年月日"){
                                            $set = "ST_";
                                        }else if($tKey == "終了年月日"){
                                            $set = "ED_";
                                        }else{
                                            $set = "";
                                        }
                                        foreach($tVal as $rKey => $rVal){
                                            $mltUpdate[$set.$date[$rKey]] = $rVal;
                                        }
                                    }
                                }
                            }
                            if($mltUpdateFlg != 0){
                                $mltUpdate["DATA_ROW"] = $cnt;
                                
                                _update($updateTable, "", "", $mltUpdate, $db, "2");
                            }
                        }
                    }
                }else if($fKey == "転校暦情報"){
                    $rirekiChange = array("履歴番号"    => "RIREKI_NO",
                                          "学校区分"    => "KUBUN",
                                          "学校名"      => "NAME",
                                          "異動区分"    => "C_KUBUN",
                                          "異動事由"    => "REASON");
                    $cnt = 1;
                    $rireki = array();
                    $rireki["REGISTERCD"] = STAFFCD;
                    $rireki["UPDATED"] = "sysdate()";
                    $rireki["SCHREGNO"] = $schregNo;
                    
                    foreach($fVal as $sKey => $sVal){
                        if(is_numeric($sKey)){
                            $rirekiUpdate = 0;
                            
                            $rireki["REGISTERCD"] = STAFFCD;
                            $rireki["UPDATED"] = "sysdate()";
                            $rireki["SCHREGNO"] = $schregNo;
                            $rireki["DATA_ROW"] = $cnt;
                            foreach($sVal as $tKey => $tVal){
                                if(array_key_exists($tKey, $rirekiChange)){
                                    $rireki[$rirekiChange[$tKey]] = $tVal;
                                }else{
                                    if($tKey == "小学校情報"){
                                        $top = "PRI_";
                                    }else{
                                        $top = "JNR_";
                                    }
                                    foreach($tVal as $rKey => $rVal){
                                        if(array_key_exists($rKey, $rirekiChange)){
                                            $rireki[$top.$rirekiChange[$rKey]] = $rVal;
                                        }else{
                                            if($rKey == "開始年月日"){
                                                $set = $top."ST_";
                                            }else{
                                                $set = $top."ED_";
                                            }
                                            foreach($rVal as $vKey => $vVal){
                                                $rireki[$set.$date[$vKey]] = $vVal;
                                            }
                                        }
                                    }
                                }
                            }
                            _update(CE_APP_G_CHANGE_DAT, "", "", $rireki, $db, "2");
                            //クリア
                            $rireki = array();
                            $cnt++;
                        }else{
                            $rirekiUpdate = 1;
                            
                            if(array_key_exists($sKey, $rirekiChange)){
                                $rireki[$rirekiChange[$sKey]] = $sVal;
                            }else{
                                if($sKey == "小学校情報"){
                                    $top = "PRI_";
                                }else{
                                    $top = "JNR_";
                                }
                                foreach($sVal as $tKey => $tVal){
                                    if(array_key_exists($tKey, $rirekiChange)){
                                        $rireki[$top.$rirekiChange[$tKey]] = $tVal;
                                    }else{
                                        if($tKey == "開始年月日"){
                                            $set = $top."ST_";
                                        }else{
                                            $set = $top."ED_";
                                        }
                                        foreach($tVal as $rKey => $rVal){
                                            $rireki[$set.$date[$rKey]] = $rVal;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if($rirekiUpdate != 0){
                        $rireki["DATA_ROW"] = $cnt;
                        
                        _update(CE_APP_G_CHANGE_DAT, "", "", $rireki, $db, "2");
                    }
                }
            }
        }

    }

    //中学生だった場合、学齢簿の学年の値を修正
    if ($jFlg && $gakureiF[$student["学年"]] < 7) {
        $gakureiF[$student["学年"]] = $gakureiF[$student["学年"]] + 6;
    }

    //CE_APP_GAKUREIBO_F_MST
    _update(CE_APP_GAKUREIBO_F_MST, "", "", $gakureiF, $db, "2");
    
    //CE_APP_GAKUREIBO_S_MST
    _update(CE_APP_GAKUREIBO_S_MST, "", "", $gakureiS, $db, "2");
    
    
    //DB切断
    Query::dbCheckIn($db);
}

//指導要録情報から基礎情報作成
function kenjaInsertShidou($field)
{
    //DB接続
    $db = Query::dbCheckOut();
    
    
    //コード値にエラーがないかチェック
    //指導要録種別取得
    $query = knjo121Query::getErrSyubetu($field["SCHREGNO"]);
    $errSyubetu = $db->getOne($query);
    $syubetu = substr($errSyubetu, 0, 1);
    
    //学校変更状況と在学学年のGRADEを取得
    $query = knjo121Query::getErrGrade($field["SCHREGNO"]);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $change = array();
        $zaigaku = array();
        if($row["CHANGE"] != $row["ZAIGAKU"]){
            //年度でくっつけて取得するので、この時点で学年が違ったらエラー
            //DB切断
            Query::dbCheckIn($db);
            return 1;   //エラー
        }else{
            //CHANGE も ZAIGAKUも同じ値のはずなのでCHANGEだけでチェックする
            $change[0] = substr($row["CHANGE"], 0, 1);
            $change[1] = substr($row["CHANGE"], 1, 1);
            
            if($syubetu != $change[0]){
                //DB切断
                Query::dbCheckIn($db);
                return 1;   //エラー
            }else{
                if($syubetu == "P"){
                    if($change[1] < 1 || $change[1] > 6){
                        //DB切断
                        Query::dbCheckIn($db);
                        return 1;   //エラー
                    }
                }else{
                    if($change[1] < 1 || $change[1] > 3){
                        //DB切断
                        Query::dbCheckIn($db);
                        return 1;   //エラー
                    }
                }
            }
            
            
        }
    }
    
    //指導に関する記録の学年のコードチェック
    $query = knjo121Query::getShidouGrade($field["SCHREGNO"]);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $shidou = array();
        
        $shidou[0] = substr($row["SHIDOU"], 0, 1);
        $shidou[1] = substr($row["SHIDOU"], 1, 1);
        
        if($syubetu != $shidou[0]){
            //DB切断
            Query::dbCheckIn($db);
            return 1;   //エラー
        }else{
            if($syubetu == "P"){
                if($shidou[1] < 1 || $shidou[1] > 6){
                    //DB切断
                    Query::dbCheckIn($db);
                    return 1;   //エラー
                }
            }else{
                if($shidou[1] < 1 || $shidou[1] > 3){
                    //DB切断
                    Query::dbCheckIn($db);
                    return 1;   //エラー
                }
            }
        }
    }
    
    //SCHREG_BASE_MSTの更新
    $query = knjo121Query::getSBaseData($field["SCHREGNO"]);
    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
    
    $baseUpdate = array();
    
    $baseUpdate["REGISTERCD"] = STAFFCD;
    $baseUpdate["UPDATED"] = "sysdate()";
    
    $baseUpdate["SCHREGNO"] = $field["SCHREGNO"];
    $baseUpdate["INOUTCD"] = "1";
    
    $baseUpdate["NAME"] = $row["NAME"];
    if($row["ALIAS"] != ""){
        $baseUpdate["NAME_SHOW"] = $row["ALIAS"];
    }else{
        $baseUpdate["NAME_SHOW"] = $row["NAME"];
    }
    $baseUpdate["NAME_KANA"] = mb_convert_kana($row["NAME_KANA"], "c");
    $baseUpdate["BIRTHDAY"] = $row["BIRTHDAY"];
    $baseUpdate["SEX"] = $row["SEX"];
    $baseUpdate["HANDICAP"] = $row["HANDICAP"];
    $baseUpdate["ENT_DATE"] = $field["ENTDATE"];
    $baseUpdate["ENT_DIV"] = "4";
    $baseUpdate["ENT_SCHOOL"] = $row["ENT_SCHOOL"];
    $baseUpdate["ENT_ADDR"] = $row["ENT_ADDR"];
    $baseUpdate["ENT_ADDR2"] = str_replace(",", "", $field["KATEI"]);
    
    _update(SCHREG_BASE_MST, "", "", $baseUpdate, $db, "2");
    
    //SCHREG_ADDRESS_DATも更新
    $addr = array();
    $addr["REGISTERCD"] = STAFFCD;
    $addr["UPDATED"] = "sysdate()";
    
    $addr["SCHREGNO"] = $field["SCHREGNO"];
    $addr["ISSUEDATE"] = CTRL_DATE;
    
    $addr["ZIPCD"] = $row["ZIPCD"];
    $addr["ADDR1"] = $row["NADDR"];
    $addr["ADDR2"] = $row["NKATAGAKI"];
    $addr["ADDR_FLG"] = "1";
    
    _update(SCHREG_ADDRESS_DAT, "", "", $addr, $db, "2");
    
    //全体に使いたい！
    $schoolKind = substr($row["SYUBETU"], 0, 1);
    
    //SCHREG_ENT_GRD_HIST_DATの更新のときに使いたい
    $entSchool = $row["ENT_SCHOOL"];
    $entAddr = $row["ENT_ADDR"];
    
    //SCHREG_REGD_DATの更新
    $regdUpdate = array();
    
    $regdUpdate["REGISTERCD"] = STAFFCD;
    $regdUpdate["UPDATED"] = "sysdate()";
    
    $regdUpdate["SCHREGNO"] = $field["SCHREGNO"];
    $regdUpdate["YEAR"] = CTRL_YEAR;
    $regdUpdate["SEMESTER"] = CTRL_SEMESTER;
    
    $grade = explode(",", $field["GRADE"]);
    $regdUpdate["GRADE"] = $grade[0];
    $regdUpdate["HR_CLASS"] = $grade[1];
    $regdUpdate["ATTENDNO"] = sprintf("%03d", $field["ATTENDNO"]);
    $regdUpdate["ANNUAL"] = sprintf("%02d", $field["ANNUAL"]);
    
    $katei = explode(",", $field["KATEI"]);
    $regdUpdate["COURSECD"] = $katei[0];
    $regdUpdate["MAJORCD"] = $katei[1];
    
    $regdUpdate["COURSECODE"] = $field["COURSE"];
    
    _update(SCHREG_REGD_DAT, "", "", $regdUpdate, $db, "2");
    
    
    //SCHREG_ENT_GRD_HIST_DATの更新
    $entUpdate = array();

    $entUpdate["REGISTERCD"] = STAFFCD;
    $entUpdate["UPDATED"] = "sysdate()";
    
    $entUpdate["SCHREGNO"] = $field["SCHREGNO"];
    $entUpdate["SCHOOL_KIND"] = $schoolKind;
    
    $entUpdate["ENT_DATE"] = $field["ENTDATE"];
    $entUpdate["ENT_DIV"] = "4";
    
    $entUpdate["ENT_SCHOOL"] = $entSchool;
    $entUpdate["ENT_ADDR"] = $entAddr;
    
    $entUpdate["ENT_ADDR2"] = str_replace(",", "", $field["KATEI"]);
    
    _update(SCHREG_ENT_GRD_HIST_DAT, "", "", $entUpdate,$db, "2");
    
    //SCHREG_ATTENDREC_DATの更新
    $query = knjo121Query::getSAttendRecData($field["SCHREGNO"], "1");
    $cnt = $db->getOne($query);
    if($cnt > 0){
        $query = knjo121Query::getSAttendRecData($field["SCHREGNO"]);
        $result = $db->query($query);
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $attend = array();
            $attend["REGISTERCD"] = STAFFCD;
            $attend["UPDATED"] = "sysdate()";
            $attend["SCHREGNO"] = $field["SCHREGNO"];
            $attend["SCHOOLCD"] = "1";
            
            $attend["YEAR"] = $row["YEAR"];
            $attend["ANNUAL"] = $row["GRADECD"];
            
            $attend["CLASSDAYS"] = $row["CLASS_CNT"];
            $attend["OFFDAYS"] = "0";
            $attend["ABSENT"] = "0";
            $attend["SUSPEND"] = $row["STOP_CNT"];
            $attend["MOURNING"] = "0";
            $attend["ABROAD"] = "0";
            if($row["MUST_CNT"] != ""){
                $attend["REQUIREPRESENT"] = $row["MUST_CNT"];
            }else{
                $attend["REQUIREPRESENT"] = $row["CLASS_CNT"] - $row["STOP_CNT"];
            }
            $attend["SICK"] = $row["ABSENCE_CNT"];
            $attend["ACCIDENTNOTICE"] = "0";
            $attend["NOACCIDENTNOTICE"] = "0";
            $attend["PRESENT"] = $row["ATTEND_CNT"];
            
            _update(SCHREG_ATTENDREC_DAT, "", "", $attend, $db, "2");
        }
        
    }
    
    //GUARDIAN_DATとGUARDIAN2_DAT、ADDRESS_DATの更新
    $query = knjo121Query::getParentData($field["SCHREGNO"]);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        //GUARDIAN_DAT用
        $guard = array();
        $guard["REGISTERCD"] = STAFFCD;
        $guard["UPDATED"] = "sysdate()";
        
        $guard["SCHREGNO"] = $field["SCHREGNO"];
        
        $guard["RELATIONSHIP"] = "00";
        $guard["GUARD_NAME"] = $row["NNAME"];
        $guard["GUARD_KANA"] = $row["NAME_KANA"];
        
        $guard["GUARD_ZIPCD"] = $row["ZIPCD"];
        $guard["GUARD_ADDR1"] = $row["NADDR"];
        $guard["GUARD_ADDR2"] = $row["NKATAGAKI"];
        
        //GUARDIAN_ADDRESS_DAT用
        $gAddr = array();
        $gAddr["REGISTERCD"] = STAFFCD;
        $gAddr["UPDATED"] = "sysdate()";
        
        $gAddr["SCHREGNO"] = $field["SCHREGNO"];
        $gAddr["ISSUEDATE"] = CTRL_DATE;
        
        $gAddr["GUARD_ZIPCD"] = $row["ZIPCD"];
        $gAddr["GUARD_ADDR1"] = $row["NADDR"];
        $gAddr["GUARD_ADDR2"] = $row["NKATAGAKI"];
        $gAddr["GUARD_ADDR_FLG"] = "1";
        
        if($row["DATA_ROW"] == "1"){
            _update(GUARDIAN_DAT, "", "", $guard, $db, "2");
            _update(GUARDIAN_ADDRESS_DAT, "", "", $gAddr, $db, "2");
        }else{
            _update(GUARDIAN2_DAT, "", "", $guard, $db, "2");
            _update(GUARDIAN2_ADDRESS_DAT, "", "", $gAddr, $db, "2");
        }
    }
    
    //DB切断
    Query::dbCheckIn($db);
    
    return 0;
}

//学齢簿情報から基礎情報作成
function kenjaInsertGaku($field)
{
    //DB接続
    $db = Query::dbCheckOut();
    
    //SCHREG_BASE_MST
    $query = knjo121Query::getGBaseData($field["SCHREGNO"]);
    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
    
    $baseUpdate = array();
    
    $baseUpdate["REGISTERCD"] = STAFFCD;
    $baseUpdate["UPDATED"] = "sysdate()";
    
    $baseUpdate["SCHREGNO"] = $field["SCHREGNO"];
    if($field["INTYPE"] == "1"){
        //入学
        $baseUpdate["INOUTCD"] = "1";
        $baseUpdate["ENT_DIV"] = "1";
    }else{
        //転入学
        $baseUpdate["INOUTCD"] = "3";
        $baseUpdate["ENT_DIV"] = "4";
    }
    
    $baseUpdate["NAME"] = $row["NAME"];
    $baseUpdate["NAME_SHOW"] = $row["NAME"];    //通称名ないのでとりあえず
    $baseUpdate["NAME_KANA"] = mb_convert_kana($row["NAME_KANA"], "c");
    $baseUpdate["BIRTHDAY"] = $row["BIRTHDAY"];
    $baseUpdate["SEX"] = $row["SEX"];
    if($row["CLASS_KUBUN"] != "0"){
        $baseUpdate["HANDICAP"] = "002";    //特別支援学級の場合
    }else{
        $baseUpdate["HANDICAP"] = "001";
    }
    $baseUpdate["ENT_DATE"] = $field["ENTDATE"];
    //$baseUpdate["ENT_DIV"] = "4";
    $baseUpdate["ENT_SCHOOL"] = "";
    $baseUpdate["ENT_ADDR"] = "";
    $baseUpdate["ENT_ADDR2"] = str_replace(",", "", $field["KATEI"]);
    
    _update(SCHREG_BASE_MST, "", "", $baseUpdate, $db, "2");
    
    //SCHREG_ADDRESS_DATも更新
    $addr = array();
    $addr["REGISTERCD"] = STAFFCD;
    $addr["UPDATED"] = "sysdate()";
    
    $addr["SCHREGNO"] = $field["SCHREGNO"];
    $addr["ISSUEDATE"] = CTRL_DATE;
    
    $addr["ZIPCD"] = $row["ZIPCD"];
    $addr["ADDR1"] = $row["NADDR"];
    $addr["ADDR2"] = $row["NKATAGAKI"];
    $addr["ADDR_FLG"] = "1";
    
    _update(SCHREG_ADDRESS_DAT, "", "", $addr, $db, "2");

    //SCHREG_REGD_DAT
    $regdUpdate = array();
    
    $regdUpdate["REGISTERCD"] = STAFFCD;
    $regdUpdate["UPDATED"] = "sysdate()";
    
    $regdUpdate["SCHREGNO"] = $field["SCHREGNO"];
    $regdUpdate["YEAR"] = CTRL_YEAR;
    $regdUpdate["SEMESTER"] = CTRL_SEMESTER;
    
    $grade = explode(",", $field["GRADE"]);
    $regdUpdate["GRADE"] = $grade[0];
    $regdUpdate["HR_CLASS"] = $grade[1];
    $regdUpdate["ATTENDNO"] = sprintf("%03d", $field["ATTENDNO"]);
    $regdUpdate["ANNUAL"] = sprintf("%02d", $field["ANNUAL"]);
    
    $katei = explode(",", $field["KATEI"]);
    $regdUpdate["COURSECD"] = $katei[0];
    $regdUpdate["MAJORCD"] = $katei[1];
    
    $regdUpdate["COURSECODE"] = $field["COURSE"];
    
    _update(SCHREG_REGD_DAT, "", "", $regdUpdate, $db, "2");

    //SCHREG_ENT_GRD_HIST_DAT
    $entUpdate = array();

    $entUpdate["REGISTERCD"] = STAFFCD;
    $entUpdate["UPDATED"] = "sysdate()";
    
    $entUpdate["SCHREGNO"] = $field["SCHREGNO"];
    if($row["S_GRADE"] < 7){
        $entUpdate["SCHOOL_KIND"] = "P";
    }else{
        $entUpdate["SCHOOL_KIND"] = "J";
    }

    //出身学校卒業情報
    if($entUpdate["SCHOOL_KIND"] == "J"){
        $entUpdate["FINISH_DATE"] = $row["PRI_GRD_YEAR"]."-".$row["PRI_GRD_MONTH"]."-".$row["PRI_GRD_MONTH"];
    }
    //課題
    //卒業した学校の学校コード（FINSCHOOL_CD）が学齢簿からは取得できないため、FINSCHOOL_MSTに登録を行えていない。

    $entUpdate["ENT_DATE"] = $field["ENTDATE"];
    if($field["INTYPE"] == "1"){
        //入学
        $entUpdate["ENT_DIV"] = "1";
    }else{
        //転入学
        $baseUpdate["ENT_DIV"] = "4";
        if($entUpdate["SCHOOL_KIND"] == "P"){
            $baseUpdate["ENT_SCHOOL"] = $row["PRI_NAME"];
        }else{
            $baseUpdate["ENT_SCHOOL"] = $row["JNR_NAME"];
        }
    }
    $entUpdate["ENT_ADDR"] = "";
    
    $entUpdate["ENT_ADDR2"] = str_replace(",", "", $field["KATEI"]);
    
    _update(SCHREG_ENT_GRD_HIST_DAT, "", "", $entUpdate,$db, "2");
    
    //GUARDIAN_DAT
    $guard = array();
    $guard["REGISTERCD"] = STAFFCD;
    $guard["UPDATED"] = "sysdate()";
    
    $guard["SCHREGNO"] = $field["SCHREGNO"];
    
    $guard["RELATIONSHIP"] = "00";
    $guard["GUARD_NAME"] = $row["P_NNAME"];
    $guard["GUARD_KANA"] = mb_convert_kana($row["P_NAME_KANA"], "c");
    
    $guard["GUARD_ZIPCD"] = $row["P_ZIPCD"];
    $guard["GUARD_ADDR1"] = $row["P_NADDR"];
    $guard["GUARD_ADDR2"] = $row["P_NKATAGAKI"];
    
    //GUARDIAN_ADDRESS_DAT用
    $gAddr = array();
    $gAddr["REGISTERCD"] = STAFFCD;
    $gAddr["UPDATED"] = "sysdate()";
    
    $gAddr["SCHREGNO"] = $field["SCHREGNO"];
    $gAddr["ISSUEDATE"] = CTRL_DATE;
    
    $gAddr["GUARD_ZIPCD"] = $row["P_ZIPCD"];
    $gAddr["GUARD_ADDR1"] = $row["P_NADDR"];
    $gAddr["GUARD_ADDR2"] = $row["P_NKATAGAKI"];
    $gAddr["GUARD_ADDR_FLG"] = "1";

    _update(GUARDIAN_DAT, "", "", $guard, $db, "2");
    _update(GUARDIAN_ADDRESS_DAT, "", "", $gAddr, $db, "2");

    //DB切断
    Query::dbCheckIn($db);
    
    return 0;
}

?>
