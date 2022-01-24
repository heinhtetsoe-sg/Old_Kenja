<?php
class knjo101Model extends Model {
    var $grade;
    var $cmd;
    var $examyear;
    var $recordTableDiv;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限


    function knjo101Model()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->valErrMess = array();
        $this->valErrFlg = "";


        $this->cmd   = VARS::request("cmd");
        $this->file = VARS::file("XML");
        $this->radio = "1";
        
        //パーツから受け取り
        $this->field = array("SCHREGNO" => VARS::post("SCHREGNO"),  //0
                             "KATEI"    => VARS::post("KATEI"),     //1
                             "COURSE"   => VARS::post("COURSE"),    //2
                             "GRADE"    => VARS::post("GRADE"),     //3
                             "ATTENDNO" => VARS::post("ATTENDNO"),  //4
                             "ANNUAL"   => VARS::post("ANNUAL"),    //5
                             "ENTDATE"  => VARS::post("ENTDATE") != "" ? str_replace("/","-",VARS::post("ENTDATE")) : date("Y-m-d"),  //6
                             );

        //パーツ作成用変数
        $this->partsCnt = 7;
        $this->parts = array();
        //学籍番号
        $this->parts[0] = array("TYPE"      =>  "text",
                                "NAME"      =>  "SCHREGNO",
                                "SIZE"      =>  10,
                                "MAXLENGTH" =>  8,
                                "EXTRA"     => "",
                                "LABEL"     =>  "学籍番号");
        //課程学科用query
        $query1 = knjo101Query::getKatei();
        $this->parts[1] = array("TYPE"      =>  "combo",
                                "NAME"      =>  "KATEI",
                                "SQL"       =>  $query1,
                                "EXTRA"     =>  "",
                                "BLANK"     =>  "BLANK",
                                "SIZE"      =>  "1",
                                "LABEL"     =>  "課程・学科");
        
        //コース用query
        $query2 = knjo101Query::getCourse();
        $this->parts[2] = array("TYPE"      =>  "combo",
                                "NAME"      =>  "COURSE",
                                "SQL"       =>  $query2,
                                "EXTRA"     =>  "",
                                "BLANK"     =>  "BLANK",
                                "SIZE"      =>  "1",
                                "LABEL"     =>  "コース");
        
        //年組用query
        $query3 = knjo101Query::getGrade();
        $this->parts[3] = array("TYPE"      =>  "combo",
                                "NAME"      =>  "GRADE",
                                "SQL"       =>  $query3,
                                "EXTRA"     =>  "",
                                "BLANK"     =>  "BLANK",
                                "SIZE"      =>  "1",
                                "LABEL"     =>  "年組");
        
        //出席番号
        $this->parts[4] = array("TYPE"      =>  "text",
                                "NAME"      =>  "ATTENDNO",
                                "SIZE"      =>  4,
                                "MAXLENGTH" =>  3,
                                "EXTRA"     => "",
                                "LABEL"     =>  "番");

        //年次
        $this->parts[5] = array("TYPE"      =>  "text",
                                "NAME"      =>  "ANNUAL",
                                "SIZE"      =>  4,
                                "MAXLENGTH" =>  2,
                                "EXTRA"     => "",
                                "LABEL"     =>  "年次");

        //日付
        $this->parts[6] = array("TYPE"      =>  "calendar",
                                "NAME"      =>  "ENTDATE",
                                "LABEL"     =>  "入学日");

        
        //ラジオボタン用の変数
        /*$this->radioName = "XSD";
        $this->radioVal = array(2, 1);
        $this->radioExtra = array("id=\"XSD1\"", "id=\"XSD2\"");
        $this->radioLabel = array("学習者情報", "学校保健");
        $this->radioLabelId = array("XSD1", "XSD2");*/
        $this->radioName = "";
        
        //画面タイトル用
        $this->title = "学習者情報";
        

    }
    
    function fileUpdate()
    {
        /*if($this->radio == ""){
            $this->setMessage("xmlの種類を選択してください");
            return false;
        }else if($this->radio == "1"){*/
            $this->xsd =  DOCUMENTROOT ."/common/XSD/"."eduAK3101s-2016-01.xsd";
        /*}else{
            $this->xsd =  DOCUMENTROOT ."/common/XSD/"."eduAK3201s-2016-01.xsd";
        }*/
        
        //ファイルをアップロードする
        $setFileName = explode(".", $this->file['name']);

        $setFileName[0] = mb_convert_kana($setFileName[0],"s","UTF-8");
        $setFileName[0] = str_replace(" ", "-", $setFileName[0]);

        $setFileName[0] = mb_convert_encoding($setFileName[0], "SJIS-win", "UTF-8");
        $filename = DOCUMENTROOT ."/common/XML/". $setFileName[0].'.'.$setFileName[count($setFileName) - 1];

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
        
        
        //要録取り込んだテーブルすべて
        $delArray = array("SCHREG_BASE_MST",
                          "SCHREG_ADDRESS_DAT",
                          "SCHREG_REGD_DAT",
                          "SCHREG_ENT_GRD_HIST_DAT",
                          "GUARDIAN_DAT",
                          "GUARDIAN_ADDRESS_DAT",
                          "GUARDIAN2_DAT",
                          "GUARDIAN2_ADDRESS_DAT",
                          "H_APP_YOUROKU_TO_MST",
                          "H_APP_YOUROKU_FROM_MST",
                          "H_APP_YOUROKU_SYUBETU_MST",
                          "H_APP_YOUROKU_IDOU_MST",
                          "H_APP_Y_SCHOOL_CHANGE_DAT",
                          "H_APP_Y_PRINCIPAL_DAT",
                          "H_APP_Y_HRTEACHER_DAT",
                          "H_APP_Y_STUDENT_ALIAS_DAT",
                          "H_APP_Y_STUDENT_OTHERADDR_DAT",
                          "H_APP_Y_PARENT_DAT",
                          "H_APP_Y_PARENT_OTHERADDR_DAT",
                          "H_APP_Y_GRADE_DAT",
                          "H_APP_Y_BACKGROUND_DAT",
                          "H_APP_Y_STAY_ABROAD_DAT",
                          "H_APP_Y_COMMON_SUBJ_DAT",
                          "H_APP_Y_EXPERT_SUBJ_DAT",
                          "H_APP_Y_SHIDOU1_DAT",
                          "H_APP_Y_SHIDOU1_COMMON_SUBJ_DAT",
                          "H_APP_Y_SHIDOU1_EXPERT_SUBJ_DAT",
                          "H_APP_Y_SHIDOU2_DAT");
        foreach($delArray as $val){
            $query = knjo101Query::delete($this->field["SCHREGNO"], $val);
            $db->query($query);
        }
        //SCHOOLCDが1のもののみ削除のテーブル
        $delsArray = array("SCHREG_ATTENDREC_DAT",
                           "SCHREG_STUDYREC_DAT",
                          );
        foreach($delsArray as $val){
            $query = knjo101Query::delete2($this->field["SCHREGNO"], $val);
            $db->query($query);
        }
        //SCHREG_TRANSFER_DAT削除
        $query = "DELETE FROM SCHREG_TRANSFER_DAT WHERE SCHREGNO = '".$this->field["SCHREGNO"]."' AND TRANSFERCD = '1'";
        $db->query($query);
        
        
        Query::dbCheckIn($db);
    }
    
    function updateModel()
    {
        
        //データを配列に
        $data = getReadModel($this);
        
        //データ更新function
        if($this->radio == "1"){
            YourokuUpdate($this->field["SCHREGNO"], $data);
        }
        
        
        //とりあえずecho
        //print_r($data);
        return true;
    }
    

    function insertModel()
    {
        //賢者用データ作成Model
        $error = kenjaInsert($this->field);
        
        if($error != 0){
            $this->setMessage("取込データのコード値に不備があるため、\\n学籍データを作成できませんでした。");
        }else{
            $this->setMessage("更新しました。");
        }
        return true;
    }

}

//学習者情報(H_APP_YOUROKU_テーブル)の更新
function YourokuUpdate($schregNo, $data) 
{
    //更新用変数
    $kyouikuTo = array();   //H_APP_YOUROKU_TO_MST更新用
    $kyouikuFrom = array(); //H_APP_YOUROKU_FROM_MST更新用
    $syubetu = array();     //H_APP_YOUROKU_SYUBETU_MST更新用
    $idou = array();        //H_APP_YOUROKU_IDOU_MST更新用
    
    //とりあえず学籍番号適当に
    $kyouikuTo["SCHREGNO"] = $schregNo;
    $kyouikuTo["REGISTERCD"] = STAFFCD;
    $kyouikuTo["UPDATED"] = "sysdate()";
    $kyouikuFrom["SCHREGNO"] = $schregNo;
    $kyouikuFrom["REGISTERCD"] = STAFFCD;
    $kyouikuFrom["UPDATED"] = "sysdate()";
    $syubetu["SCHREGNO"] = $schregNo;
    $syubetu["REGISTERCD"] = STAFFCD;
    $syubetu["UPDATED"] = "sysdate()";
    $idou["SCHREGNO"] = $schregNo;
    $idou["REGISTERCD"] = STAFFCD;
    $idou["UPDATED"] = "sysdate()";
    
    //DB接続
    $db = Query::dbCheckOut();
    
    
    //変換用配列   XMLから作成した配列のKEY => 更新するテーブルのフィールド名
    $Kyouiku = array("教育委員会ID"             =>  "EDU_ID",
                     "外字教育委員会名"         =>  "EDU_GNAME",
                     "内字教育委員会名"         =>  "EDU_NNAME",
                     "部課係"                   =>  "EDU_SECTION",
                     "住所コード"         =>  "EDU_ADDRCD",
                     "外字住所"           =>  "EDU_GADDR",
                     "内字住所"           =>  "EDU_NADDR",
                     "外字方書"           =>  "EDU_GKATAGAKI",
                     "内字方書"           =>  "EDU_NKATAGAKI",
                     "郵便番号"           =>  "EDU_ZIPCD",
                     "電話番号"                 =>  "EDU_TELNO",
                     "職"                       =>  "EDU_SYOKU");
    $Gakko = array("学校ID"             =>  "SCHOOL_ID",
                   "外字学校名"         =>  "SCHOOL_GNAME",
                   "内字学校名"         =>  "SCHOOL_NNAME",
                   "住所コード"   =>  "SCHOOL_ADDRCD",
                   "外字住所"     =>  "SCHOOL_GADDR",
                   "内字住所"     =>  "SCHOOL_NADDR",
                   "外字方書"     =>  "SCHOOL_GKATAGAKI",
                   "内字方書"     =>  "SCHOOL_NKATAGAKI",
                   "郵便番号"     =>  "SCHOOL_ZIPCD",
                   "分校ID"             =>  "SCHOOL_BUN_ID",
                   "外字分校名"         =>  "SCHOOL_BUN_GNAME",
                   "内字分校名"         =>  "SCHOOL_BUN_NNAME",
                   "電話番号"           =>  "SCHOOL_TELNO",
                   "職"                 =>  "SCHOOL_SYOKU",
                   "備考"               =>  "SCHOOL_BIKOU");
    $Bunko = array("住所コード"   =>  "SCHOOL_BUN_ADDRCD",
                   "外字住所"     =>  "SCHOOL_BUN_GADDR",
                   "内字住所"     =>  "SCHOOL_BUN_NADDR",
                   "外字方書"     =>  "SCHOOL_BUN_GKATAGAKI",
                   "内字方書"     =>  "SCHOOL_BUN_NKATAGAKI",
                   "郵便番号"     =>  "SCHOOL_BUN_ZIPCD");
    
    $Name = array("外字氏名"      => "GNAME",
                  "内字氏名"      => "NNAME",
                  "フリガナ"      => "NAME_KANA");
    
    $Addr = array("住所コード"    => "ADDRCD",
                  "外字住所"      => "GADDR",
                  "内字住所"      => "NADDR",
                  "外字方書"      => "GKATAGAKI",
                  "内字方書"      => "NKATAGAKI",
                  "郵便番号"      => "ZIPCD",
                  "備考"                => "BIKOU");
                  
    $Kyouka = array("教科"              => "CLASS_NAME",
                    "科目名"            => "SUBCLASS_NAME",
                    "修得単位数"        => "SUBCLASS_TANNI",    //学校設定科目内の修得単位のときはSCHOOL_をくっつける
                    "学校設定教科"      => "SCHOOL_CLASS_NAME",
                    "学校設定科目名"    => "SCHOOL_SUBCLASS_NAME");
                  
    $shiSubChange = array("教科"                        => "CLASS_NAME",
                          "科目名"                      => "SUBCLASS_NAME",
                          "評定"                        => "SUBCLASS_HYOUTEI",
                          "修得単位数"                  => "SUBCLASS_TANNI",
                          "修得単位数の計"              => "SUBCLASS_TANNI_TOTAL",
                          "備考"                        => "SUBCLASS_BIKOU",
                          "学校設定教科"                => "SCHOOL_CLASS_NAME",
                          "学校設定科目名"              => "SCHOOL_SUBCLASS_NAME",
                          "学校設定科目評定"            => "SCHOOL_SUBCLASS_HYOUTEI",
                          "学校設定科目修得単位数"      => "SCHOOL_SUBCLASS_TANNI",
                          "学校設定科目修得単位数の計"  => "SCHOOL_SUBCLASS_TANNI_TOTAL",
                          "学校設定科目備考"            => "SCHOOL_SUBCLASS_BIKOU");
                          
    //指導に関する記録1で削除する項目の配列
    $shiSubClear = array("CLASS_NAME", 
                         "SUBCLASS_NAME", 
                         "SUBCLASS_HYOUTEI",
                         "SUBCLASS_TANNI",
                         "SUBCLASS_TANNI_TOTAL",
                         "SUBCLASS_BIKOU",
                         "SCHOOL_CLASS_NAME", 
                         "SCHOOL_SUBCLASS_NAME", 
                         "SCHOOL_SUBCLASS_HYOUTEI",
                         "SCHOOL_SUBCLASS_TANNI",
                         "SCHOOL_SUBCLASS_TANNI_TOTAL",
                         "SCHOOL_SUBCLASS_BIKOU");
    
    
    //利用業務ユニット・送信先・送信元・指導要録情報で分岐
    foreach($data as $fkey => $fval){
        if($fkey == "利用業務ユニット"){
            
            //更新テーブルはH_APP_YOUROKU_TO_MST
            $kyouikuTo["GYOUMU_UNIT"] = $fval;
            
        }else if($fkey == "送信先情報"){
            //更新テーブルはH_APP_YOUROKU_TO_MST
            foreach($fval as $toKey => $toVal){
                if($toKey == "教育委員会"){
                    foreach($toVal as $tkKey => $tkVal){
                        if($tkKey != "所在地"){
                            
                            $kyouikuTo[$Kyouiku[$tkKey]] = $tkVal;
                            
                        }else{
                            foreach($tkVal as $tksKey => $tksVal){
                                
                                $kyouikuTo[$Kyouiku[$tksKey]] = $tksVal;
                                
                            }
                        }
                        
                    }
                }else{      //学校
                    foreach($toVal as $tgKey => $tgVal){
                        if($tgKey != "所在地" && $tgKey != "分校所在地"){
                            
                            $kyouikuTo[$Gakko[$tgKey]] = $tgVal;
                            
                        }else if($tgKey == "所在地"){
                            foreach($tgVal as $tgsKey => $tgsVal){
                                
                                $kyouikuTo[$Gakko[$tgsKey]] = $tgsVal;
                                
                            }
                        }else{  //分校所在地
                            foreach($tgVal as $tgbKey => $tgbVal){
                                
                                $kyouikuTo[$Bunko[$tgbKey]] = $tgbVal;
                                
                            }
                        }
                    }
                }
            }
            
        }else if($fkey == "送信元情報"){
            //更新テーブルはH_APP_YOUROKU_FROM_MST
            foreach($fval as $fromKey => $fromVal){
                if($fromKey == "教育委員会"){
                    foreach($fromVal as $fkKey => $fkVal){
                        if($fkKey != "所在地"){
                            
                            $kyouikuFrom[$Kyouiku[$fkKey]] = $fkVal;
                            
                        }else{
                            foreach($fkVal as $fksKey => $fksVal){
                                
                                $kyouikuFrom[$Kyouiku[$fksKey]] = $fksVal;
                                
                            }
                        }
                        
                    }
                }else{      //学校
                    foreach($fromVal as $fgKey => $fgVal){
                        if($fgKey != "所在地" && $fgKey != "分校所在地"){
                            
                            $kyouikuFrom[$Gakko[$fgKey]] = $fgVal;
                            
                        }else if($fgKey == "所在地"){
                            foreach($fgVal as $fgsKey => $fgsVal){
                                
                                $kyouikuFrom[$Gakko[$fgsKey]] = $fgsVal;
                                
                            }
                        }else{  //分校所在地
                            foreach($fgVal as $fgbKey => $fgbVal){
                                
                                $kyouikuFrom[$Bunko[$fgbKey]] = $fgbVal;
                                
                            }
                        }
                    }
                }
            }
            
        }else if($fkey == "送信日時"){
            
            //更新テーブルはH_APP_YOUROKU_FROM_MST
            //Tを半角スペースに変換
            $send = str_replace("T", " ", $fval);
            //+から上だけ使う
            $send_date = explode("+", $send);
            
            //$kyouikuFrom["SEND_DATE"] = $send_date[0].".000000";
            //$kyouikuFrom["SEND_DATE"] = $fval;
            //$kyouikuFrom["SEND_DATE"] = "sysdate()";
            //$timestamp = new DateTime($send_date[0]);                   //日付をオブジェクトに
            //$date_array = (array)$timestamp;                            //オブジェクトを配列に
            //$kyouikuFrom["SEND_DATE"] = "'".$date_array["date"]."'";    //"'"で囲まないとエラーになってしまう

            $kyouikuFrom["SEND_DATE"] = "'".$send_date[0].".000000'";   //とか色々やってみたけど･･･結局はこういうことだった。

        }else{  //指導要録情報
            foreach($fval as $yKey => $yVal){
                if($yKey == "指導要録種別"){
                    //更新はH_APP_YOUROKU_SYUBETU_MST
                    $syubetu["YOUROKU_SYUBETU"] = $yVal;
                }else if($yKey == "学校変更状況"){
                    $schcng = 1;
                    //学校変更状況内の置き換え用配列
                    $schoolChange = array("年度"            => "YEAR",
                                          "教育課程の区分"  => "KUBUN",
                                          "学年"            => "GRADE",
                                          "原級留置回数"    => "STAY_CNT",
                                          );
                                          
                    $school = array();
                    $school["REGISTERCD"] = STAFFCD;
                    $school["UPDATED"] = "sysdate()";
                    $school["SCHREGNO"] = $schregNo;
                    $school["DATA_ROW"] = $schcng;
                    
                    
                    //校長・ホームルーム担任用のROWNO
                    $koutyou = 1;
                    $homeroom = 1;
                    foreach($yVal as $gkKey => $gkVal){
                        if(is_numeric($gkKey)){
                            $schoolUpdate = 0;
                            
                            $school["DATA_ROW"] = $schcng;
                            
                            foreach($gkVal as $gkfKey => $gkfVal){
                                if($gkfKey == "校長氏名"){
                                    $principal = array();
                                    $principal["REGISTERCD"] = STAFFCD;
                                    $principal["UPDATED"] = "sysdate()";
                                    $principal["SCHREGNO"] = $schregNo;
                                    
                                    $principal["UPPER_DATA_ROW"] = $schcng;   //親の番号
                                    
                                    foreach($gkfVal as $gksKey => $gksVal){
                                        if(is_numeric($gksKey)){
                                            $principalUpdate = 0;
                                            $principal["DATA_ROW"] = $koutyou;  //自分の番号
                                            
                                            foreach($gksVal["氏名"] as $gktKey => $gktVal){
                                                $principal[$Name[$gktKey]] = $gktVal;
                                                
                                            }
                                            
                                            _update(H_APP_Y_PRINCIPAL_DAT, "", "", $principal, $db, "2");
                                            
                                            $koutyou++;
                                            
                                        }else{
                                            $principalUpdate = 1;
                                            foreach($gksVal as $gktKey => $gktVal){
                                                $principal[$Name[$gktKey]] = $gktVal;
                                                
                                            }
                                        }
                                        
                                    }
                                    if($principalUpdate != 0){
                                        $principal["DATA_ROW"] = $koutyou;  //自分の番号
                                        _update(H_APP_Y_PRINCIPAL_DAT, "", "", $principal, $db, "2");
                                    }
                                    
                                }else if($gkfKey == "ホームルーム担任者氏名"){
                                    $hrteacher = array();
                                    $hrteacher["REGISTERCD"] = STAFFCD;
                                    $hrteacher["UPDATED"] = "sysdate()";
                                    $hrteacher["SCHREGNO"] = $schregNo;
                                    
                                    $hrteacher["UPPER_DATA_ROW"] = $schcng;
                                    
                                    foreach($gkfVal as $gksKey => $gksVal){
                                        if(is_numeric($gksKey)){
                                            $hrteacherUpdate = 0;
                                            $hrteacher["DATA_ROW"] = $homeroom;  //自分の番号
                                            
                                            foreach($gksVal["氏名"] as $gktKey => $gktVal){
                                                $hrteacher[$Name[$gktKey]] = $gktVal;
                                                
                                            }
                                            
                                            _update(H_APP_Y_HRTEACHER_DAT, "", "", $hrteacher, $db, "2");
                                            
                                            $homeroom++;
                                            
                                        }else{
                                            $hrteacherUpdate = 1;
                                            foreach($gksVal as $gktKey => $gktVal){
                                                $hrteacher[$Name[$gktKey]] = $gktVal;
                                                
                                            }
                                        }
                                        
                                    }
                                    if($hrteacherUpdate != 0){
                                        $hrteacher["DATA_ROW"] = $homeroom;
                                        _update(H_APP_Y_HRTEACHER_DAT, "", "", $hrteacher, $db, "2");
                                    }

                                }else{
                                    $school[$schoolChange[$gkfKey]] = $gkfVal;
                                }
                            }
                            _update(H_APP_Y_SCHOOL_CHANGE_DAT, "", "", $school, $db, "2");
                            
                            //クリア
                            $school["STAY_CNT"] = "";
                            
                            $schcng++;
                            
                        }else{
                            $schoolUpdate = 1;
                            if($gkKey == "校長氏名"){
                                $principal = array();
                                $principal["REGISTERCD"] = STAFFCD;
                                $principal["UPDATED"] = "sysdate()";
                                
                                $principal["UPPER_DATA_ROW"] = $schcng;   //親の番号
                                $principal["DATA_ROW"] = $koutyou;  //自分の番号
                                
                                foreach($gkVal as $gkfKey => $gkfVal){
                                    if(is_numeric($gkfKey)){
                                        $principalUpdate = 0;
                                        $principal["DATA_ROW"] = $koutyou;  //自分の番号
                                        
                                        foreach($gkfVal["氏名"] as $gksKey => $gksVal){
                                            $principal[$Name[$gksKey]] = $gksVal;
                                            
                                        }
                                        
                                        _update(H_APP_Y_PRINCIPAL_DAT, "", "", $principal, $db, "2");
                                        
                                        $koutyou++;
                                        
                                    }else{
                                        $principalUpdate = 1;
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            $principal[$Name[$gksKey]] = $gksVal;
                                            
                                        }
                                    }
                                    
                                }
                                if($principalUpdate != 0){
                                    $principal["SCHREGNO"] = $schregNo;
                                    _update(H_APP_Y_PRINCIPAL_DAT, "", "", $principal, $db, "2");
                                }
                                
                            }else if($gkKey == "ホームルーム担任者氏名"){
                                $homeroom++;
                                $hrteacher = array();
                                $hrteacher["REGISTERCD"] = STAFFCD;
                                $hrteacher["UPDATED"] = "sysdate()";
                                
                                $hrteacher["UPPER_DATA_ROW"] = $schcng;
                                $hrteacher["DATA_ROW"] = $homeroom;
                                
                                foreach($gkVal as $gkfKey => $gkfVal){
                                    if(is_numeric($gkfKey)){
                                        $hrteacherUpdate = 0;
                                        $hrteacher["DATA_ROW"] = $homeroom;  //自分の番号
                                        
                                        foreach($gkfVal["氏名"] as $gksKey => $gksVal){
                                            $hrteacher[$Name[$gksKey]] = $gksVal;
                                            
                                        }
                                        
                                        _update(H_APP_Y_HRTEACHER_DAT, "", "", $hrteacher, $db, "2");
                                        
                                        $homeroom++;
                                        
                                    }else{
                                        $hrteacherUpdate = 1;
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            $hrteacher[$Name[$gksKey]] = $gksVal;
                                            
                                        }
                                    }
                                    
                                }
                                if($hrteacherUpdate != 0){
                                    $hrteacher["SCHREGNO"] = $schregNo;
                                    _update(H_APP_Y_HRTEACHER_DAT, "", "", $hrteacher, $db, "2");
                                }

                            }else{
                                $school[$schoolChange[$gkKey]] = $gkVal;
                            }
                        }
                    }
                    if($schoolUpdate != 0){
                        _update(H_APP_Y_SCHOOL_CHANGE_DAT, "", "", $school, $db, "2");
                    }
                    
                }else if($yKey == "学籍の記録"){
                    foreach($yVal as $gkKey => $gkVal){
                        if($gkKey == "生徒" || $gkKey == "在学状況"){
                            //学籍の記録＞生徒・在籍状況の置き換え配列(複数テーブルのは別にする)
                            $gksyubetu = array("生徒ID"             =>  "STUDENT_ID",
                                               "性別"               =>  "STUDENT_SEX",
                                               "生年月日"           =>  "STUDENT_BIRTHDAY",
                                               "国籍"               =>  "STUDENT_NATION",
                                               "外字氏名"     =>  "STUDENT_GNAME",
                                               "内字氏名"     =>  "STUDENT_NNAME",
                                               "フリガナ"     =>  "STUDENT_KANA",
                                               "住所コード"   =>  "STUDENT_ADDRCD",
                                               "外字住所"     =>  "STUDENT_GADDR",
                                               "内字住所"     =>  "STUDENT_NADDR",
                                               "外字方書"     =>  "STUDENT_GKATAGAKI",
                                               "内字方書"     =>  "STUDENT_NKATAGAKI",
                                               "郵便番号"     =>  "STUDENT_ZIPCD");
                            
                            foreach($gkVal as $gkfKey => $gkfVal){
                                if(array_key_exists($gkfKey, $gksyubetu)){
                                
                                    //更新はH_APP_YOUROKU_SYUBETU_MST
                                    $syubetu[$gksyubetu[$gkfKey]] = $gkfVal;
                                    
                                }else{ 
                                    if($gkfKey == "氏名"){
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            //更新はH_APP_YOUROKU_SYUBETU_MST
                                            $syubetu[$gksyubetu[$gksKey]] = $gksVal;
                                        }
                                    }else if($gkfKey == "通称名"){
                                        $aliasCnt = 1;
                                        $alias = array();
                                        $alias["REGISTERCD"] = STAFFCD;
                                        $alias["UPDATED"] = "sysdate()";
                                        $alias["SCHREGNO"] = $schregNo;
                                        
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if(is_numeric($gksKey)){
                                                $aliasUpdate = 0;
                                                $alias["DATA_ROW"] = $aliasCnt;
                                                foreach($gksVal as $gktKey => $gktVal){
                                                    $alias[$Name[$gktKey]] = $gktVal;
                                                }
                                                
                                                _update(H_APP_Y_STUDENT_ALIAS_DAT, "", "", $alias, $db, "2");
                                                
                                                $aliasCnt++;
                                                
                                            }else{
                                                $alias[$Name[$gksKey]] = $gksVal;
                                                
                                                $aliasUpdate = 1;
                                            }
                                        }
                                        if($aliasUpdate != 0){
                                            $alias["DATA_ROW"] = $aliasCnt;
                                            
                                            _update(H_APP_Y_STUDENT_ALIAS_DAT, "", "", $alias, $db, "2");

                                        }
                                    }else if($gkfKey == "現住所"){
                                                        
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if($gksKey != "備考"){
                                                foreach($gksVal as $gktKey => $gktVal){
                                                    if(array_key_exists($gktKey, $gksyubetu)){
                                                        $syubetu[$gksyubetu[$gktKey]] = $gktVal;
                                                    }
                                                }
                                                
                                            }else{  //備考
                                                $syubetu["STUDENT_BIKOU"] = $gksVal;
                                            }
                                        }
                                    }else if($gkfKey == "現住所_その他"){
                                        $stuAddCnt = 1;
                                        $stuAddOtr = array();
                                        $stuAddOtr["REGISTERCD"] = STAFFCD;
                                        $stuAddOtr["UPDATED"] = "sysdate()";
                                        $stuAddOtr["SCHREGNO"] = $schregNo;
                                        
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if(is_numeric($gksKey)){
                                                $stuAddUpdate = 0;
                                                $stuAddOtr["DATA_ROW"] = $stuAddCnt;
                                                foreach($gksVal["住所"] as $gktKey => $gktVal){
                                                    $stuAddOtr[$Addr[$gktKey]] = $gktVal;
                                                }
                                                if(array_key_exists("備考", $gksVal)){
                                                    $stuAddOtr["BIKOU"] = $gksVal["備考"];
                                                }
                                                _update(H_APP_Y_STUDENT_OTHERADDR_DAT, "", "", $stuAddOtr, $db, "2");
                                                
                                                //クリア
                                                $stuAddOtr["BIKOU"] = "";
                                                
                                                $stuAddCnt++;
                                                
                                            }else{
                                                if($gksKey == "住所"){
                                                    foreach($gksVal as $gktKey => $gktVal){
                                                        $stuAddOtr[$Addr[$gktKey]] = $gktVal;
                                                    }
                                                }else{
                                                    $stuAddOtr[$Addr[$gksKey]] = $gksVal;
                                                }
                                                
                                                $stuAddUpdate = 1;
                                            }
                                        }
                                        if($stuAddUpdate != 0){
                                            $stuAddOtr["DATA_ROW"] = $stuAddCnt;
                                            
                                            _update(H_APP_Y_STUDENT_OTHERADDR_DAT, "", "", $stuAddOtr, $db, "2");

                                        }
                                    }else if($gkfKey == "保護者"){
                                        $parentCnt = 1;
                                        $parent = array();
                                        $parent["REGISTERCD"] = STAFFCD;
                                        $parent["UPDATED"] = "sysdate()";
                                        $parent["SCHREGNO"] = $schregNo;
                                        
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if(is_numeric($gksKey)){
                                                $parentUpdate = 0;
                                                $parent["DATA_ROW"] = $parentCnt;
                                                
                                                foreach($gksVal as $gktKey => $gktVal){

                                                    if($gktKey == "保護者ID"){
                                                        $parent["PARENT_ID"] = $gktVal;
                                                    }else if($gktKey == "氏名"){
                                                        foreach($gktVal as $gkrKey => $gkrVal){
                                                            $parent[$Name[$gkrKey]] = $gkrVal;
                                                        }
                                                    }else if($gktKey == "現住所"){
                                                        foreach($gktVal as $gkrKey => $gkrVal){
                                                            if($gkrKey == "住所"){
                                                                foreach($gkrVal as $gkvKey => $gkvVal){
                                                                    $parent[$Addr[$gkvKey]] = $gkvVal;
                                                                }
                                                            }else{
                                                                $parent[$Addr[$gkrKey]] = $gkrVal;
                                                            }
                                                        }
                                                    }else if($gktKey == "現住所_その他"){
                                                        $parAddCnt = 1;
                                                        $parAddOtr = array();
                                                        $parAddOtr["REGISTERCD"] = STAFFCD;
                                                        $parAddOtr["UPDATED"] = "sysdate()";
                                                        $parAddOtr["SCHREGNO"] = $schregNo;
                                                        $parAddOtr["UPPER_DATA_ROW"] = $parentCnt;
                                                        
                                                        foreach($gktVal as $gkrKey => $gkrVal){
                                                            if(is_numeric($gkrKey)){
                                                                $parAddUpdate = 0;
                                                                $parAddOtr["DATA_ROW"] = $parAddCnt;
                                                                
                                                                foreach($gkrVal as $gkvKey => $gkvVal){
                                                                    if($gkvKey == "住所"){
                                                                        foreach($gkvVal as $gkxKey => $gkxVal){
                                                                            $parAddOtr[$Addr[$gkxKey]] = $gkxVal;
                                                                        }
                                                                    }else{
                                                                        $parAddOtr[$Addr[$gkvKey]] = $gkvVal;
                                                                    }
                                                                }
                                                                _update(H_APP_Y_PARENT_OTHERADDR_DAT, "", "", $parAddOtr, $db, "2");
                                                                
                                                                //クリア
                                                                $parAddOtr["BIKOU"] = "";
                                                                
                                                                $parAddCnt++;
                                                                
                                                            }else{
                                                                $parAddUpdate = 1;
                                                                $parAddOtr["DATA_ROW"] = $parAddCnt;
                                                                
                                                                if($gkrKey == "住所"){
                                                                    foreach($gkrVal as $gkvKey => $gkvVal){
                                                                        $parAddOtr[$Addr[$gkvKey]] = $gkvVal;
                                                                    }
                                                                }else{
                                                                    $parAddOtr[$Addr[$gkrKey]] = $gkrVal;
                                                                }
                                                            }
                                                        }
                                                        if($parAddUpdate != 0){
                                                            _update(H_APP_Y_PARENT_OTHERADDR_DAT, "", "", $parAddOtr, $db, "2");
                                                        }
                                                    }


                                                }
                                                
                                                _update(H_APP_Y_PARENT_DAT, "", "", $parent, $db, "2");
                                                
                                                //クリア
                                                $parent["PARENT_ID"] = "";
                                                
                                                $parentCnt++;
                                            }else{
                                                $parentUpdate = 1;
                                                $parent["DATA_ROW"] = $parentCnt;
                                                
                                                if($gksKey == "保護者ID"){
                                                    $parent["PARENT_ID"] = $gksVal;
                                                }else if($gksKey == "氏名"){
                                                    foreach($gksVal as $gktKey => $gktVal){
                                                        $parent[$Name[$gktKey]] = $gktVal;
                                                    }
                                                }else if($gksKey == "現住所"){
                                                    foreach($gksVal as $gktKey => $gktVal){
                                                        if($gktKey == "住所"){
                                                            foreach($gktVal as $gkrKey => $gkrVal){
                                                                $parent[$Addr[$gkrKey]] = $gkrVal;
                                                            }
                                                        }else{
                                                            $parent[$Addr[$gktKey]] = $gktVal;
                                                        }
                                                    }
                                                }else if($gksKey == "現住所_その他"){
                                                    $parAddCnt = 1;
                                                    $parAddOtr = array();
                                                    $parAddOtr["REGISTERCD"] = STAFFCD;
                                                    $parAddOtr["UPDATED"] = "sysdate()";
                                                    $parAddOtr["SCHREGNO"] = $schregNo;
                                                    $parAddOtr["UPPER_DATA_ROW"] = $parentCnt;
                                                    
                                                    foreach($gksVal as $gktKey => $gktVal){
                                                        if(is_numeric($gktKey)){
                                                            $parAddUpdate = 0;
                                                            $parAddOtr["DATA_ROW"] = $parAddCnt;
                                                            
                                                            foreach($gktVal as $gkrKey => $gkrVal){
                                                                if($gkrKey == "住所"){
                                                                    foreach($gkrVal as $gkvKey => $gkvVal){
                                                                        $parAddOtr[$Addr[$gkvKey]] = $gkvVal;
                                                                    }
                                                                }else{
                                                                    $parAddOtr[$Addr[$gkrKey]] = $gkrVal;
                                                                }
                                                            }
                                                            _update(H_APP_Y_PARENT_OTHERADDR_DAT, "", "", $parAddOtr, $db, "2");
                                                            
                                                            //クリア
                                                            $parAddOtr["BIKOU"] = "";
                                                            
                                                            $parAddCnt++;
                                                            
                                                        }else{
                                                            $parAddUpdate = 1;
                                                            $parAddOtr["DATA_ROW"] = $parAddCnt;
                                                            
                                                            if($gktKey == "住所"){
                                                                foreach($gktVal as $gkrKey => $gkrVal){
                                                                    $parAddOtr[$Addr[$gkrKey]] = $gkrVal;
                                                                }
                                                            }else{
                                                                $parAddOtr[$Addr[$gktKey]] = $gktVal;
                                                            }
                                                        }
                                                    }
                                                    if($parAddUpdate != 0){
                                                        _update(H_APP_Y_PARENT_OTHERADDR_DAT, "", "", $parAddOtr, $db, "2");
                                                    }
                                                }
                                            }
                                        }
                                        if($parentUpdate != 0){
                                            _update(H_APP_Y_PARENT_DAT, "", "", $parent, $db, "2");
                                        }
                                        
                                    }else if($gkfKey == "学校情報"){
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if($gksKey != "所在地" && $gksKey != "分校所在地"){
                                                
                                                $syubetu[$Gakko[$gksKey]] = $gksVal;
                                                
                                            }else if($gksKey == "所在地"){
                                                foreach($gksVal as $gktKey => $gktVal){
                                                    
                                                    $syubetu[$Gakko[$gktKey]] = $gktVal;
                                                    
                                                }
                                            }else{  //分校所在地
                                                foreach($gksVal as $gktKey => $gktVal){
                                                    
                                                    $syubetu[$Bunko[$gktKey]] = $gktVal;
                                                    
                                                }
                                            }
                                        }
                                    }else if($gkfKey == "在学学年"){
                                        //変換用
                                        $gradeChange = array("年度"             => "YEAR",
                                                             "学年または年次"   => "GRADE",
                                                             "ホームルーム"     => "HR_CLASS",
                                                             "整理番号"         => "ATTENDNO");
                                        
                                        $gradeCnt = 1;
                                        $grade = array();
                                        $grade["REGISTERCD"] = STAFFCD;
                                        $grade["UPDATED"] = "sysdate()";
                                        $grade["SCHREGNO"] = $schregNo;
                                        
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if(is_numeric($gksKey)){
                                                $gradeUpdate = 0;
                                                $grade["DATA_ROW"] = $gradeCnt;
                                                
                                                foreach($gksVal as $gktKey => $gktVal){
                                                    $grade[$gradeChange[$gktKey]] = $gktVal;
                                                }
                                                
                                                _update(H_APP_Y_GRADE_DAT, "", "", $grade, $db, "2");
                                                $gradeCnt++;
                                            }else{
                                                $gradeUpdate = 1;
                                                
                                                $grade[$gradeChange[$gksKey]] = $gksVal;
                                            }
                                        }
                                        if($gradeUpdate != 0){
                                            $grade["DATA_ROW"] = $gradeCnt;
                                            
                                            _update(H_APP_Y_GRADE_DAT, "", "", $grade, $db, "2");
                                        }
                                    }
                                }
                            }
                            
                        }else if($gkKey == "異動状況" || $gkKey == "課程名" || $gkKey == "学科名"){
                            //学籍の記録＞異動状況・課程名・学科名の置き換え配列
                            $gkidou = array("入学年月日"                => "IN_DATE",
                                            "編入学年月日"              => "IN_DATE",
                                            "編入学時学年"              => "IN_GRADE",
                                            "転入学年月日"              => "IN_DATE",
                                            "転入学時学年"              => "IN_GRADE",
                                            "在学すべき期間_開始年月日" => "ZAIGAKU_FROM",
                                            "在学すべき期間_終了年月日" => "ZAIGAKU_TO",
                                            "備考"                      => "_BIKOU",   //入学側の備考ならENTつける。出る側ならOUT
                                            "転学年月日"                => "OUT_DATE",
                                            "退学年月日"                => "OUT_DATE",
                                            "卒業年月日"                => "OUT_DATE",
                                            "学校を去った日"            => "LEAVE_DATE",
                                            "進学先就職先等"            => "AFTER_LEAVE",
                                            "課程名"                    => "KATEI_NAME",
                                            "学科名"                    => "GAKKA_NAME");
                                            
                            //更新はH_APP_YOUROKU_IDOU_MST
                            if($gkKey == "異動状況"){
                                foreach($gkVal as $gkfKey => $gkfVal){
                                    if($gkfKey == "入学" || $gkfKey == "編入学" || $gkfKey == "転入学"){
                                        $bikou = "ENT";
                                        if($gkfKey == "入学"){
                                            $idou["IN_FLG"] = 1;
                                        }else if($gkfKey == "編入学"){
                                            $idou["IN_FLG"] = 2;
                                        }else if($gkfKey == "転入学"){
                                            $idou["IN_FLG"] = 3;
                                        }
                                        
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if($gksKey != "備考"){
                                                $idou[$gkidou[$gksKey]] = $gksVal;
                                            }else{
                                                $idou[$bikou.$gkidou[$gksKey]] = $gksVal;
                                            }
                                        }
                                        
                                    }else if($gkfKey == "転学" || $gkfKey == "退学" || $gkfKey == "卒業"){
                                        $bikou = "OUT";
                                        if($gkfKey == "転学"){
                                            $idou["OUT_FLG"] = 1;
                                        }else if($gkfKey == "退学"){
                                            $idou["OUT_FLG"] = 2;
                                        }else if($gkfKey == "卒業"){
                                            $idou["OUT_FLG"] = 3;
                                        }
                                        
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if($gksKey != "備考"){
                                                $idou[$gkidou[$gksKey]] = $gksVal;
                                            }else{
                                                $idou[$bikou.$gkidou[$gksKey]] = $gksVal;
                                            }
                                        }
                                        
                                    }else if($gkfKey == "進学先就職先等"){
                                        $idou[$gkidou[$gkfKey]] = $gkfVal;
                                    }else if($gkfKey == "入学前の経歴"){
                                        $backCnt = 1;
                                        $back = array();
                                        $back["REGISTERCD"] = STAFFCD;
                                        $back["UPDATED"] = "sysdate()";
                                        $back["SCHREGNO"] = $schregNo;
                                        
                                        if(is_array($gkfVal)){
                                            foreach($gkfVal as $gksKey => $gksVal){
                                                $back["DATA_ROW"] = $backCnt;
                                                
                                                $back["KEIREKI"] = $gksVal;
                                                
                                                _update(H_APP_Y_BACKGROUND_DAT, "", "", $back, $db, "2");
                                                $backCnt++;
                                            }
                                            
                                        }else{
                                            $back["DATA_ROW"] = $backCnt;
                                            
                                            $back["KEIREKI"] = $gkfVal;
                                            
                                            _update(H_APP_Y_BACKGROUND_DAT, "", "", $back, $db, "2");
                                        }
                                        
                                    }else if($gkfKey == "留学等"){
                                        //変換用
                                        $stayChange = array("留学開始年月日" => "START_DATE",
                                                            "留学終了年月日" => "END_DATE",
                                                            "備考"           => "BIKOU");
                                                            
                                        $stayCnt = 1;
                                        $stay = array();
                                        $stay["REGISTERCD"] = STAFFCD;
                                        $stay["UPDATED"] = "sysdate()";
                                        $stay["SCHREGNO"] = $schregNo;
                                        
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if(is_numeric($gksKey)){
                                                $stayUpdate = 0;
                                                
                                                $stay["DATA_ROW"] = $stayCnt;
                                                
                                                foreach($gksVal as $gktKey => $gktVal){
                                                    $stay[$stayChange[$gktKey]] = $gktVal;
                                                }
                                                
                                                _update(H_APP_Y_STAY_ABROAD_DAT, "", "", $stay, $db, "2");
                                                
                                                //クリア
                                                $stay["BIKOU"] = "";
                                                
                                                $stayCnt++;
                                            }else{
                                                $stayUpdate = 1;
                                                
                                                $stay[$stayChange[$gksKey]] = $gksVal;
                                            }
                                        }
                                        if($stayUpdate != 0){
                                            $stay["DATA_ROW"] = $stayCnt;
                                            _update(H_APP_Y_STAY_ABROAD_DAT, "", "", $stay, $db, "2");
                                        }
                                    }
                                }
                            }else{
                                $idou[$gkidou[$gkKey]] = $gkVal;
                            }
                        }
                    }
                }else if($yKey == "各教科_科目等の修得単位数の記録"){
                    
                    foreach($yVal as $gkKey => $gkVal){
                        if($gkKey == "各学科に共通する各教科_科目"){
                            $comSubCnt = 1;
                            $comSub = array();
                            $comSub["REGISTERCD"] = STAFFCD;
                            $comSub["UPDATED"] = "sysdate()";
                            $comSub["SCHREGNO"] = $schregNo;
                            
                            foreach($gkVal as $gkfKey => $gkfVal){
                                if(is_numeric($gkfKey)){
                                    $comSubUpdate = 0;
                                    $comSub["DATA_ROW"] = $comSubCnt;
                                    
                                    foreach($gkfVal as $gksKey => $gksVal){
                                        if($gksKey == "教科" || $gksKey == "学校設定教科"){
                                            $comSub[$Kyouka[$gksKey]] = $gksVal;
                                        }else if($gksKey != "学校設定科目"){
                                            foreach($gksVal as $gktKey => $gktVal){
                                                $comSub[$Kyouka[$gktKey]] = $gktVal;
                                            }
                                        }else{
                                            $comSub["SCHOOL_SUBCLASS_NAME"] = $gksVal["学校設定科目名"];
                                            $comSub["SCHOOL_SUBCLASS_TANNI"] = $gksVal["修得単位数"];
                                        }
                                    }
                                    _update(H_APP_Y_COMMON_SUBJ_DAT, "", "", $comSub, $db, "2");
                                    //クリアしたい
                                    $comSub["CLASS_NAME"] = "";
                                    $comSub["SUBCLASS_NAME"] = "";
                                    $comSub["SUBCLASS_TANNI"] = "";
                                    $comSub["SCHOOL_CLASS_NAME"] = "";
                                    $comSub["SCHOOL_SUBCLASS_NAME"] = "";
                                    $comSub["SCHOOL_SUBCLASS_TANNI"] = "";
                                    
                                    $comSubCnt++;
                                }else{
                                    $comSubUpdate = 1;
                                    if($gkfKey == "教科" || $gkfKey == "学校設定教科"){
                                        $comSub[$Kyouka[$gkfKey]] = $gkfVal;
                                    }else if($gkfKey != "学校設定科目"){
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            $comSub[$Kyouka[$gksKey]] = $gksVal;
                                        }
                                    }else{
                                        $comSub["SCHOOL_SUBCLASS_NAME"] = $gkfVal["学校設定科目名"];
                                        $comSub["SCHOOL_SUBCLASS_TANNI"] = $gkfVal["修得単位数"];
                                    }
                                    
                                }
                            }
                            if($comSubUpdate != 0){
                                $comSub["DATA_ROW"] = $comSubCnt;
                                _update(H_APP_Y_COMMON_SUBJ_DAT, "", "", $comSub, $db, "2");
                            }
                        }else if($gkKey == "主として専門学科において開設される各教科_科目"){
                            $exSubCnt = 1;
                            $exSub = array();
                            $exSub["REGISTERCD"] = STAFFCD;
                            $exSub["UPDATED"] = "sysdate()";
                            $exSub["SCHREGNO"] = $schregNo;
                            
                            foreach($gkVal as $gkfKey => $gkfVal){
                                if(is_numeric($gkfKey)){
                                    $exSubUpdate = 0;
                                    $exSub["DATA_ROW"] = $exSubCnt;
                                    
                                    foreach($gkfVal as $gksKey => $gksVal){
                                        if($gksKey == "教科" || $gksKey == "学校設定教科"){
                                            $exSub[$Kyouka[$gksKey]] = $gksVal;
                                        }else if($gksKey != "学校設定科目"){
                                            foreach($gksVal as $gktKey => $gktVal){
                                                $exSub[$Kyouka[$gktKey]] = $gktVal;
                                            }
                                        }else{
                                            $exSub["SCHOOL_SUBCLASS_NAME"] = $gksVal["学校設定科目名"];
                                            $exSub["SCHOOL_SUBCLASS_TANNI"] = $gksVal["修得単位数"];
                                        }
                                    }
                                    _update(H_APP_Y_EXPERT_SUBJ_DAT, "", "", $exSub, $db, "2");
                                    //クリアしたい
                                    $exSub["CLASS_NAME"] = "";
                                    $exSub["SUBCLASS_NAME"] = "";
                                    $exSub["SUBCLASS_TANNI"] = "";
                                    $exSub["SCHOOL_CLASS_NAME"] = "";
                                    $exSub["SCHOOL_SUBCLASS_NAME"] = "";
                                    $exSub["SCHOOL_SUBCLASS_TANNI"] = "";
                                    
                                    $exSubCnt++;
                                }else{
                                    $exSubUpdate = 1;
                                    if($gkfKey == "教科" || $gkfKey == "学校設定教科"){
                                        $exSub[$Kyouka[$gkfKey]] = $gkfVal;
                                    }else if($gkfKey != "学校設定科目"){
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            $exSub[$Kyouka[$gksKey]] = $gksVal;
                                        }
                                    }else{
                                        $exSub["SCHOOL_SUBCLASS_NAME"] = $gkfVal["学校設定科目名"];
                                        $exSub["SCHOOL_SUBCLASS_TANNI"] = $gkfVal["修得単位数"];
                                    }
                                    
                                }
                            }
                            if($exSubUpdate != 0){
                                $exSub["DATA_ROW"] = $exSubCnt;
                                _update(H_APP_Y_EXPERT_SUBJ_DAT, "", "", $exSub, $db, "2");
                            }
                        }else if($gkKey == "留学"){
                            $idou["ABROAD_TANNI"] = $gkVal["修得単位数"];
                        }else if($gkKey == "自立活動"){
                            $idou["ZIRITSU_TANNI"] = $gkVal["修得単位数"];
                        }else if($gkKey == "総合的な学習の時間"){
                            $integArray = array("総合的な学習の名称"    => "INTEGRATED_NAME",
                                                "修得単位数"            => "INTEGRATED_TANNI");
                            foreach($gkVal as $gkfKey => $gksVal){
                                $idou[$integArray[$gkfKey]] = $gksVal;
                            }
                        }
                    }
                }else if($yKey == "指導に関する記録1"){
                    //指導に関する記録1＞複数以外の項目変換配列
                    $shiOne = array("様式種別"                      => "SYUBETU",
                                    "学年"                          => "GRADE",
                                    "原級留置回数"                  => "STAY_CNT",
                                    "修得単位数"                    => "_TANNI", 
                                    "学習活動"                      => "INTEGRATED_ACTION",
                                    "評価"                          => "INTEGRATED_HYOUKA",
                                    "活動の状況"                    => "SPECIAL_ACTION",
                                    "自立の状況"                    => "ZIRITU_ACTION",
                                    "事項_事実_所見"                => "SYOKEN",
                                    "授業日数"                      => "CLASS_CNT",
                                    "出席停止_忌引等の日数"         => "STOP_CNT",
                                    "留学中の授業日数"              => "ABROAD_CNT",
                                    "出席しなければならない日数"    => "MUST_CNT",
                                    "欠席日数"                      => "ABSENCE_CNT",
                                    "出席日数"                      => "ATTEND_CNT",
                                    "備考"                          => "BIKOU");
                                    
                    $shiOneClear = array("STAY_CNT",
                                         "ABROAD_TANNI",
                                         "INTEGRATED_TANNI",
                                         "INTEGRATED_ACTION",
                                         "INTEGRATED_HYOUKA",
                                         "SPECIAL_ACTION",
                                         "ZIRITU_ACTION",
                                         "SYOKEN",
                                         "MST_CNT",
                                         "BIKOU");
                    
                    $firShiCnt = 1;
                    $firShi = array();
                    $firShi["REGISTERCD"] = STAFFCD;
                    $firShi["UPDATED"] = "sysdate()";
                    $firShi["SCHREGNO"] = $schregNo;
                    
                    $commonCnt = 1;
                    $expertCnt = 1;
                    foreach($yVal as $gkKey => $gkVal){
                        if(is_numeric($gkKey)){
                            $firShiUpdate = 0;
                            
                            $firShi["DATA_ROW"] = $firShiCnt;
                            
                            foreach($gkVal as $gkfKey => $gkfVal){
                                if($gkfKey != "各学科に共通する各教科_科目" && $gkfKey != "主として専門学科において開設される各教科_科目"){
                                    if(is_array($gkfVal)){
                                        if($gkfKey == "留学"){
                                            $top = "ABROAD";
                                        }else if($gkfKey == "総合的な学習の時間の記録"){
                                            $top = "INTEGRATED";
                                        }else{
                                            $top = "";
                                        }
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if($gksKey != "修得単位数"){
                                                $firShi[$shiOne[$gksKey]] = $gksVal;
                                            }else{
                                                $firShi[$top.$shiOne[$gksKey]] = $gksVal;
                                            }
                                        }
                                    }else{
                                        $firShi[$shiOne[$gkfKey]] = $gkfVal;
                                    }
                                }else if($gkfKey == "各学科に共通する各教科_科目"){
                                    $shiCSub = array();
                                    $shiCSub["REGISTERCD"] = STAFFCD;
                                    $shiCSub["UPDATED"] = "sysdate()";
                                    $shiCSub["SCHREGNO"] = $schregNo;
                                    $shiCSub["UPPER_DATA_ROW"] = $firShiCnt;
                                    
                                    foreach($gkfVal as $gksKey => $gksVal){
                                        if(is_numeric($gksKey)){
                                            $shiCSupdate = 0;
                                            
                                            $shiCSub["DATA_ROW"] = $commonCnt;
                                            
                                            foreach($gksVal as $gktKey => $gktVal){
                                                if($gktKey == "教科" || $gktKey == "学校設定教科"){
                                                    $shiCSub[$shiSubChange[$gktKey]] = $gktVal;
                                                }else{
                                                    foreach($gktVal as $gkrKey => $gkrVal){
                                                        $shiCSub[$shiSubChange[$gkrKey]] = $gkrVal;
                                                    }
                                                }
                                            }
                                            _update(H_APP_Y_SHIDOU1_COMMON_SUBJ_DAT, "", "", $shiCSub, $db, "2");
                                            //クリア
                                            foreach($shiSubClear as $clrVal){
                                                $shiCSub[$clrVal] = "";
                                            }
                                            $commonCnt++;
                                            
                                        }else{
                                            $shiCSupdate = 1;
                                            if($gksKey == "教科" || $gksKey == "学校設定教科"){
                                                $shiCSub[$shiSubChange[$gksKey]] = $gksVal;
                                            }else{
                                                foreach($gksVal as $gktKey => $gktVal){
                                                    $shiCSub[$shiSubChange[$gktKey]] = $gktVal;
                                                }
                                            }
                                        }
                                        
                                    }
                                    if($shiCSupdate != 0){
                                        $shiCSub["DATA_ROW"] = $commonCnt;
                                        
                                        _update(H_APP_Y_SHIDOU1_COMMON_SUBJ_DAT, "", "", $shiCSub, $db, "2");
                                    }
                                    
                                }else if($gkfKey == "主として専門学科において開設される各教科_科目"){
                                    $shiESub = array();
                                    $shiESub["REGISTERCD"] = STAFFCD;
                                    $shiESub["UPDATED"] = "sysdate()";
                                    $shiESub["SCHREGNO"] = $schregNo;
                                    $shiESub["UPPER_DATA_ROW"] = $firShiCnt;
                                    
                                    foreach($gkfVal as $gksKey => $gksVal){
                                        if(is_numeric($gksKey)){
                                            $shiESupdate = 0;
                                            
                                            $shiESub["DATA_ROW"] = $expertCnt;
                                            
                                            foreach($gksVal as $gktKey => $gktVal){
                                                if($gktKey == "教科" || $gktKey == "学校設定教科"){
                                                    $shiESub[$shiSubChange[$gktKey]] = $gktVal;
                                                }else{
                                                    foreach($gktVal as $gkrKey => $gkrVal){
                                                        $shiESub[$shiSubChange[$gkrKey]] = $gkrVal;
                                                    }
                                                }
                                            }
                                            
                                            _update(H_APP_Y_SHIDOU1_EXPERT_SUBJ_DAT, "", "", $shiESub, $db, "2");
                                            //クリア
                                            foreach($shiSubClear as $clrVal){
                                                $shiESub[$clrVal] = "";
                                            }
                                            $expertCnt++;
                                            
                                        }else{
                                            $shiESupdate = 1;
                                            
                                            if($gksKey == "教科" || $gksKey == "学校設定教科"){
                                                $shiESub[$shiSubChange[$gksKey]] = $gksVal;
                                            }else{
                                                foreach($gksVal as $gktKey => $gktVal){
                                                    $shiESub[$shiSubChange[$gktKey]] = $gktVal;
                                                }
                                            }
                                            
                                        }
                                    }
                                    if($shiESupdate != 0){
                                        $shiESub["DATA_ROW"] = $expertCnt;
                                        
                                        _update(H_APP_Y_SHIDOU1_EXPERT_SUBJ_DAT, "", "", $shiESub, $db, "2");
                                    }
                                }
                            }
                            _update(H_APP_Y_SHIDOU1_DAT, "", "", $firShi, $db, "2");
                            //クリア
                            foreach($shiOneClear as $clrVal){
                                $firShi[$clrVal] = "";
                            }
                            $firShiCnt++;
                            
                        }else{
                            $firShiUpdate = 1;
                            
                            if($gkKey != "各学科に共通する各教科_科目" && $gkKey != "主として専門学科において開設される各教科_科目"){
                                if(is_array($gkVal)){
                                    if($gkKey == "留学"){
                                        $top = "ABROAD";
                                    }else if($gkKey == "総合的な学習の時間の記録"){
                                        $top = "INTEGRATED";
                                    }else{
                                        $top = "";
                                    }
                                    foreach($gkVal as $gkfKey => $gkfVal){
                                        if($gkfKey != "修得単位数"){
                                            $firShi[$shiOne[$gkfKey]] = $gkfVal;
                                        }else{
                                            $firShi[$top.$shiOne[$gkfKey]] = $gkfVal;
                                        }
                                    }
                                }else{
                                    $firShi[$shiOne[$gkKey]] = $gkVal;
                                }
                                
                            }else if($gkKey == "各学科に共通する各教科_科目"){
                                $shiCSub = array();
                                $shiCSub["REGISTERCD"] = STAFFCD;
                                $shiCSub["UPDATED"] = "sysdate()";
                                $shiCSub["SCHREGNO"] = $schregNo;
                                $shiCSub["UPPER_DATA_ROW"] = $firShiCnt;
                                
                                foreach($gkVal as $gkfKey => $gkfVal){
                                    if(is_numeric($gkfKey)){
                                        $shiCSupdate = 0;
                                        
                                        $shiCSub["DATA_ROW"] = $commonCnt;
                                        
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if($gksKey == "教科" || $gksKey == "学校設定教科"){
                                                $shiCSub[$shiSubChange[$gksKey]] = $gksVal;
                                            }else{
                                                foreach($gksVal as $gktKey => $gktVal){
                                                    $shiCSub[$shiSubChange[$gktKey]] = $gktVal;
                                                }
                                            }
                                        }
                                        _update(H_APP_Y_SHIDOU1_COMMON_SUBJ_DAT, "", "", $shiCSub, $db, "2");
                                        
                                        //クリア
                                        foreach($shiSubClear as $clrVal){
                                            $shiCSub[$clrVal] = "";
                                        }
                                        $commonCnt++;
                                        
                                    }else{
                                        $shiCSupdate = 1;
                                        
                                        if($gkfKey == "教科" || $gkfKey == "学校設定教科"){
                                            $shiCSub[$shiSubChange[$gkfKey]] = $gkfVal;
                                        }else{
                                            foreach($gkfVal as $gksKey => $gksVal){
                                                $shiCSub[$shiSubChange[$gksKey]] = $gksVal;
                                            }
                                        }
                                    }
                                }
                                if($shiCSupdate != 0){
                                    $shiCSub["DATA_ROW"] = $commonCnt;
                                    
                                    _update(H_APP_Y_SHIDOU1_COMMON_SUBJ_DAT, "", "", $shiCSub, $db, "2");
                                }
                                
                            }else if($gkKey == "主として専門学科において開設される各教科_科目"){
                                $shiESub = array();
                                $shiESub["REGISTERCD"] = STAFFCD;
                                $shiESub["UPDATED"] = "sysdate()";
                                $shiESub["SCHREGNO"] = $schregNo;
                                $shiESub["UPPER_DATA_ROW"] = $firShiCnt;
                                
                                foreach($gkVal as $gkfKey => $gkfVal){
                                    if(is_numeric($gkfKey)){
                                        $shiESupdate = 0;
                                        
                                        $shiESub["DATA_ROW"] = $expertCnt;
                                        
                                        foreach($gkfVal as $gksKey => $gksVal){
                                            if($gksKey == "教科" || $gksKey == "学校設定教科"){
                                                $shiESub[$shiSubChange[$gksKey]] = $gksVal;
                                            }else{
                                                foreach($gksVal as $gktKey => $gktVal){
                                                    $shiESub[$shiSubChange[$gktKey]] = $gktVal;
                                                }
                                            }
                                        }
                                        _update(H_APP_Y_SHIDOU1_EXPERT_SUBJ_DAT, "", "", $shiESub, $db, "2");
                                        
                                        //クリア
                                        foreach($shiSubClear as $clrVal){
                                            $shiESub[$clrVal] = "";
                                        }
                                        $expertCnt++;
                                        
                                    }else{
                                        $shiESupdate = 1;
                                        
                                        if($gkfKey == "教科" || $gkfKey == "学校設定教科"){
                                            $shiESub[$shiSubChange[$gkfKey]] = $gkfVal;
                                        }else{
                                            foreach($gkfVal as $gksKey => $gksVal){
                                                $shiESub[$shiSubChange[$gksKey]] = $gksVal;
                                            }
                                        }
                                    }
                                }
                                if($shiESupdate != 0){
                                    $shiESub["DATA_ROW"] = $expertCnt;
                                    
                                    _update(H_APP_Y_SHIDOU1_EXPERT_SUBJ_DAT, "", "", $shiESub, $db, "2");
                                }
                                
                            }
                        }
                        
                    }
                    if($firShiUpdate != 0){
                        $firShi["DATA_ROW"] = $firShiCnt;
                        
                        _update(H_APP_Y_SHIDOU1_DAT, "", "", $firShi, $db, "2");
                    }
                    
                }else if($yKey == "指導に関する記録2"){
                    //変換用
                    $secShiChange = array("様式種別"                    => "SYUBETU",
                                          "学年"                        => "GRADE",
                                          "原級留置回数"                => "STAY_CNT",
                                          "記録"                        => "RECORD",
                                          "総授業時数"                  => "TOTAL_CLASS_CNT",
                                          "学習活動"                    => "ACTION",
                                          "評価"                        => "HYOUKA",
                                          "事項_事実_所見"              => "SYOKEN",
                                          "授業日数"                    => "CLASS_CNT",
                                          "出席停止_忌引等の日数"       => "STOP_CNT",
                                          "留学中の授業日数"            => "ABROAD_CLASS_CNT",
                                          "出席しなければならない日数"  => "MUST_CNT",
                                          "欠席日数"                    => "ABSENCE_CNT",
                                          "出席日数"                    => "ATTEND_CNT",
                                          "備考"                        => "BIKOU");
                    
                    //クリア用
                    $secShiClear = array("STAY_CNT",
                                         "RECORD",
                                         "TOTAL_CLASS_CNT",
                                         "ACTION",
                                         "HYOUKA",
                                         "SYOKEN",
                                         "MUST_CNT",
                                         "BIKOU");
                    
                    $secShiCnt = 1;
                    $secShi = array();
                    $secShi["REGISTERCD"] = STAFFCD;
                    $secShi["UPDATED"] = "sysdate()";
                    $secShi["SCHREGNO"] = $schregNo;
                    
                    foreach($yVal as $gkKey => $gkVal){
                        if(is_numeric($gkKey)){
                            $secShiUpdate = 0;
                            
                            $secShi["DATA_ROW"] = $secShiCnt;
                            
                            foreach($gkVal as $gkfKey => $gkfVal){
                                if(is_array($gkfVal)){
                                    foreach($gkfVal as $gksKey => $gksVal){
                                        if($gksVal != ""){
                                            if(!is_array($gksVal)){
                                                $secShi[$secShiChange[$gksKey]] = $gksVal;
                                            }
                                        }
                                    }
                                    
                                }else{
                                    $secShi[$secShiChange[$gkfKey]] = $gkfVal;
                                }
                            }
                            
                            _update(H_APP_Y_SHIDOU2_DAT, "", "", $secShi, $db, "2");
                            
                            //クリア
                            foreach($secShiClear as $clrVal){
                                $secShi[$clrVal] = "";
                            }
                            
                            $secShiCnt++;
                        }else{
                            $secShiUpdate = 1;
                            
                            if(is_array($gkVal)){
                                foreach($gkVal as $gkfKey => $gkfVal){
                                    $secShi[$secShiChange[$gkfKey]] = $gkfVal;
                                }
                            }else{
                                $secShi[$secShiChange[$gkKey]] = $gkVal;
                            }
                            
                        }
                    }
                    if($secShiUpdate != 0){
                        $secShi["DATA_ROW"] = $secShiCnt;
                        
                        _update(H_APP_Y_SHIDOU2_DAT, "", "", $secShi, $db, "2");
                    }
                    
                                          
                }else if($yKey == "入学時の障害の状態"){
                    $idou["HANDICAP"] = $yVal["障害の状態"];
                }
            }
            
        }
    }
    
    
    //MSTのテーブルを更新する
    //H_APP_YOUROKU_TO_MST
    _update(H_APP_YOUROKU_TO_MST, "", "", $kyouikuTo, $db, "2");
    
    //H_APP_YOUROKU_FROM_MST
    _update(H_APP_YOUROKU_FROM_MST, "", "", $kyouikuFrom, $db, "2");
    
    //H_APP_YOUROKU_SYUBETU_MST
    _update(H_APP_YOUROKU_SYUBETU_MST, "", "", $syubetu, $db, "2");
    
    //H_APP_YOUROKU_IDOU_MST
    _update(H_APP_YOUROKU_IDOU_MST, "", "", $idou, $db, "2");
    
    //DB切断
    Query::dbCheckIn($db);
    
    //$this->setMessage("更新しました。");
    
    
    //print_r($kyouikuTo);
    //echo "<BR><BR>";
    //print_r($kyouikuFrom);
    //echo "<BR><BR>";
    //print_r($syubetu);
    //echo "<BR><BR>";
    //print_r($idou);
    //echo "<BR><BR>";
}

function kenjaInsert($field)
{
    //DB接続
    $db = Query::dbCheckOut();
    
    //取り込んだデータのコード値のエラーチェック
    //学校変更状況の学年と在学学年の学年のチェック
    $query = knjo101Query::getSchGrade($field["SCHREGNO"]);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $change = array();
        $zaigaku = array();
        if($row["ZAIGAKU"] != $row["CHANGE"]){
            //DB切断
            Query::dbCheckIn($db);
            return 1;   //エラー
        }else{
            if($row["KUBUN"] != "S2"){
                //学年制
                $change[0] = substr($row["CHANGE"], 0, 1);
                $change[1] = substr($row["CHANGE"], 1, 1);
                
                $zaigaku[0] = substr($row["ZAIGAKU"], 0, 1);
                $zaigaku[1] = substr($row["ZAIGAKU"], 1, 1);
                
                if($change[0] != "H" || $zaigaku[0] != "H"){
                    //DB切断
                    Query::dbCheckIn($db);
                    return 1;   //エラー
                }else{
                    if($change[1] < 1 || $change[1] > 3 || $zaigaku[1] < 1 || $zaigaku[1] > 3){
                        //DB切断
                        Query::dbCheckIn($db);
                        return 1;
                    }
                }
            }else{
                //単位制
                $change[0] = substr($row["CHANGE"], 0, 1);
                
                $zaigaku[0] = substr($row["ZAIGAKU"], 0, 1);
                
                if(!preg_match("/^[0-9]+$/", $change[0]) || !preg_match("/^[0-9]+$/", $zaigaku[0])){
                    //DB切断
                    Query::dbCheckIn($db);
                    return 1;   //エラー
                }else{
                    $change[1] = number_format($row["CHANGE"]);
                    $zaigaku[1] = number_format($row["ZAIGAKU"]);
                    
                    if($change[1] < 1 || $change[1] > 10 || $zaigaku[1] < 1 || $zaigaku[1] > 10){
                        //DB切断
                        Query::dbCheckIn($db);
                        return 1;
                    }
                }
            }
        }
    }
    //指導に関する記録の学年のコードが規格外になっていないかチェック
    $query = knjo101Query::getShidou($field["SCHREGNO"]);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $shidou = array();
        if($row["SHIDOU1"] != ""){
            if(preg_match("/^[0-9]+$/", $row["SHIDOU1"])){
                //年次
                $shidou[0] = number_format($row["SHIDOU1"]);
                if($shidou[0] < 1 || $shidou[0] > 10){
                    //DB切断
                    Query::dbCheckIn($db);
                    return 1;
                }
            }else{
                //学年
                $shidou[0] = substr($row["SHIDOU1"], 0, 1);
                $shidou[1] = substr($row["SHIDOU1"], 1, 1);
                if($shidou[0] != "H" || $shidou[1] < 1 || $shidou[1] > 3){
                    //DB切断
                    Query::dbCheckIn($db);
                    return 1;
                }
            }
        }else if($row["SHIDOU2"] != ""){
            if(preg_match("/^[0-9]+$/", $row["SHIDOU2"])){
                //年次
                $shidou[0] = number_format($row["SHIDOU2"]);
                if($shidou[0] < 1 || $shidou[0] > 10){
                    //DB切断
                    Query::dbCheckIn($db);
                    return 1;
                }
            }else{
                //学年
                $shidou[0] = substr($row["SHIDOU2"], 0, 1);
                $shidou[1] = substr($row["SHIDOU2"], 1, 1);
                if($shidou[0] != "H" || $shidou[1] < 1 || $shidou[1] > 3){
                    //DB切断
                    Query::dbCheckIn($db);
                    return 1;
                }
            }
        }
    }
    //指導に関する記録にあって在学学年にないデータがあったらエラーにする
    $query = knjo101Query::getShiGrade($field["SCHREGNO"]);
    $cnt = $db->getOne($query);
    if($cnt > 0){
        //DB切断
        Query::dbCheckIn($db);
        return 1;
    }
    
    //SCHREG_BASE_MSTの更新
    $query = knjo101Query::getBaseData($field["SCHREGNO"]);
    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
    
    $baseUpdate = array();
    
    $baseUpdate["REGISTERCD"] = STAFFCD;
    $baseUpdate["UPDATED"] = "sysdate()";
    
    $baseUpdate["SCHREGNO"] = $field["SCHREGNO"];
    $baseUpdate["INOUTCD"] = "1";
    
    $baseUpdate["NAME"] = $row["STUDENT_NNAME"];
    if($row["ALIAS"] != ""){
        $baseUpdate["NAME_SHOW"] = $row["ALIAS"];
    }else{
        $baseUpdate["NAME_SHOW"] = $row["STUDENT_NNAME"];
    }
    $baseUpdate["NAME_KANA"] = mb_convert_kana($row["STUDENT_KANA"], "c");
    $baseUpdate["BIRTHDAY"] = $row["STUDENT_BIRTHDAY"];
    $baseUpdate["SEX"] = $row["STUDENT_SEX"];
    $baseUpdate["HANDICAP"] = $row["HANDICAP"];
    //$baseUpdate["ENT_DATE"] = date("Y-m-d", strtotime("{$row["OUT_DATE"]} + 1days"));
    $baseUpdate["ENT_DATE"] = $field["ENTDATE"];
    $baseUpdate["ENT_DIV"] = "4";
    $baseUpdate["ENT_SCHOOL"] = $row["SCHOOL_NNAME"];
    $baseUpdate["ENT_ADDR"] = $row["SCHOOL_NADDR"].$row["SCHOOL_NKATAGAKI"];
    $baseUpdate["END_ADDR2"] = str_replace(",", "", $field["KATEI"]);
    
    _update(SCHREG_BASE_MST, "", "", $baseUpdate, $db, "2");
    
    //SCHREG_ADDRESS_DATの更新
    $address = array();
    $address["REGISTERCD"] = STAFFCD;
    $address["UPDATED"] = "sysdate()";
    $address["SCHREGNO"] = $field["SCHREGNO"];
    $address["ISSUEDATE"] = CTRL_DATE;
    $address["ZIPCD"] = $row["STUDENT_ZIPCD"];
    $address["ADDR1"] = $row["STUDENT_NADDR"];
    $address["ADDR2"] = $row["STUDENT_NKATAGAKI"];
    
    _update(SCHREG_ADDRESS_DAT, "", "", $address, $db, "2");
    
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
    $query = knjo101Query::getEntHistData($field["SCHREGNO"]);
    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
    
    $entUpdate = array();

    $entUpdate["REGISTERCD"] = STAFFCD;
    $entUpdate["UPDATED"] = "sysdate()";
    
    $entUpdate["SCHREGNO"] = $field["SCHREGNO"];
    $entUpdate["SCHOOL_KIND"] = "H";
    
    //$entUpdate["ENT_DATE"] = date("Y-m-d", strtotime("{$row["OUT_DATE"]} + 1days"));
    $entUpdate["ENT_DATE"] = $field["ENTDATE"];
    $entUpdate["ENT_DIV"] = "4";
    
    $entUpdate["ENT_SCHOOL"] = $row["SCHOOL_NNAME"];
    $entUpdate["ENT_ADDR"] = $row["SCHOOL_NADDR"].$row["SCHOOL_NKATAGAKI"];
    
    $entUpdate["ENT_ADDR2"] = str_replace(",", "", $field["KATEI"]);
    
    _update(SCHREG_ENT_GRD_HIST_DAT, "", "", $entUpdate,$db, "2");
    
    //SCHREG_ATTENDREC_DATの更新
    $query = knjo101Query::getAttendRecData($field["SCHREGNO"], "1");   //H_APP_Y_SHIDOU1_DATにデータがあるかカウント
    $cnt = $db->getOne($query);
    if($cnt > 0){
        //H_APP_SHIDOU1_DATからデータ取得
        $query = knjo101Query::getAttendRecData($field["SCHREGNO"]);
        $result = $db->query($query);
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $attend = array();
            $attend["REGISTERCD"] = STAFFCD;
            $attend["UPDATED"] = "sysdate()";
            $attend["SCHREGNO"] = $field["SCHREGNO"];
            $attend["SCHOOLCD"] = "1";
            
            $attend["YEAR"] = $row["YEAR"];
            //ANNUALはH_APP_SHIDOU1_DATの学年をそのまま数字に変換したものを入れればよい？
            //$attend["ANNUAL"] = $row["GRADECD"];
            $attend["ANNUAL"] = $row["GAKUNEN"];
            
            $attend["CLASSDAYS"] = $row["CLASS_CNT"];
            $attend["OFFDAYS"] = "0";
            $attend["ABSENT"] = "0";
            $attend["SUSPEND"] = $row["STOP_CNT"];
            $attend["MOURNING"] = "0";
            $attend["ABROAD"] = $row["ABROAD_CNT"];
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
    }else{
        //H_APP_SHIDOU2_DATからデータ取得する
        $query = knjo101Query::getAttendRecData2($field["SCHREGNO"], "1");  //念のためカウント
        $tCnt = $db->getOne($query);
        if($tCnt > 0){
            $query = knjo101Query::getAttendRecData2($field["SCHREGNO"]);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $attend = array();
                $attend["REGISTERCD"] = STAFFCD;
                $attend["UPDATED"] = "sysdate()";
                $attend["SCHREGNO"] = $field["SCHREGNO"];
                $attend["SCHOOLCD"] = "1";
                
                $attend["YEAR"] = $row["YEAR"];
                //$attend["ANNUAL"] = $row["GRADECD"];
                $attend["ANNUAL"] = $row["GAKUNEN"];
                
                $attend["CLASSDAYS"] = $row["CLASS_CNT"];
                $attend["OFFDAYS"] = "0";
                $attend["ABSENT"] = "0";
                $attend["SUSPEND"] = $row["STOP_CNT"];
                $attend["MOURNING"] = "0";
                $attend["ABROAD"] = $row["ABROAD_CLASS_CNT"];
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
    }
    
    //GUARDIAN_DAT,GUARDIAN_ADDRESS_DAT, GUARDIAN2_DAT, GUARDIAN2_ADDRESS_DATの更新
    $query = knjo101Query::getParentData($field["SCHREGNO"]);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
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
    
    //H_APP_Y_COMMON_SUBJ_DATとH_APP_Y_EXPERT_DATに賢者の教科科目コードを割り当て
    //まずはH_APP_Y_COMMON_SUBJ_DAT
    $query = knjo101Query::getCommonSubj($field["SCHREGNO"], "1");
    $cnt = $db->getOne($query);
    if($cnt > 0){
        $query = knjo101Query::getCommonSubj($field["SCHREGNO"]);
        $result = $db->query($query);
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $query = knjo101Query::getSubclassCd($row["SUBCLASS_NAME"]);
            $subRow = $db->getRow($query, DB_FETCHMODE_ASSOC);  //複数行取れても1行しか使わない
            
            if(!empty($subRow)){
                $update["CLASSCD"] = $subRow["CLASSCD"];
                $update["SUBCLASSCD"] = $subRow["SUBCLASSCD"];
                $update["CURRICULUM_CD"] = $subRow["CURRICULUM_CD"];
                
                $where = " WHERE SCHREGNO = '".$field["SCHREGNO"]."' AND CLASS_NAME = '".$row["CLASS_NAME"]."' AND SUBCLASS_NAME = '".$row["SUBCLASS_NAME"]."' ";
                
                _update(H_APP_Y_COMMON_SUBJ_DAT, "", $where, $update, $db, "1");
            }
        }
        
    }
    //H_APP_Y_EXPERT_SUBJ_DAT
    $query = knjo101Query::getExpertSubj($field["SCHREGNO"], "1");
    $cnt = $db->getOne($query);
    if($cnt > 0){
        $query = knjo101Query::getExpertSubj($field["SCHREGNO"]);
        $result = $db->query($query);
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $query = knjo101Query::getSubclassCd($row["SUBCLASS_NAME"]);
            $subRow = $db->getRow($query, DB_FETCHMODE_ASSOC);  //複数行取れても1行しか使わない
            
            if(!empty($subRow)){
                $update["CLASSCD"] = $subRow["CLASSCD"];
                $update["SUBCLASSCD"] = $subRow["SUBCLASSCD"];
                $update["CURRICULUM_CD"] = $subRow["CURRICULUM_CD"];
                
                $where = " WHERE SCHREGNO = '".$field["SCHREGNO"]."' AND CLASS_NAME = '".$row["CLASS_NAME"]."' AND SUBCLASS_NAME = '".$row["SUBCLASS_NAME"]."' ";
                
                _update(H_APP_Y_EXPERT_SUBJ_DAT, "", $where, $update, $db, "1");
            }
        }
        
    }
    
    
    
    //SCHREG_TRANSFER_DAT作成する
    $query = knjo101Query::getTransfer($field["SCHREGNO"], "1");
    $cnt = $db->getOne($query);
    if($cnt > 0){
        $query = knjo101Query::getTransfer($field["SCHREGNO"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $abroad = array();
            $abroad["REGISTERCD"] = STAFFCD;
            $abroad["UPDATED"] = "sysdate()";
            $abroad["SCHREGNO"] = $field["SCHREGNO"];
            $abroad["TRANSFERCD"] = "1";
            
            //開始時の年度と終了時の年度が同じかどうか確認する。
            //START
            $stDate = str_replace("-", "/", $row["START_DATE"]);
            $stYear = common::DateConv1($stDate, 12);
            //END
            $edDate = str_replace("-", "/", $row["END_DATE"]);
            $edYear = common::DateConv1($edDate, 12);
            
            if($stYear == $edYear){     //同じなら1レコード
                $abroad["TRANSFER_SDATE"] = $row["START_DATE"];
                $abroad["TRANSFER_EDATE"] = $row["END_DATE"];
                
                _update(SCHREG_TRANSFER_DAT, "", "", $abroad, $db, "2");
                
            }else{
                //1レコード目
                $abroad["TRANSFER_SDATE"] = $row["START_DATE"];
                $abroad["TRANSFER_EDATE"] = ($stYear+1).'-03-31';
                
                _update(SCHREG_TRANSFER_DAT, "", "", $abroad, $db, "2");
                
                //2レコード目
                $abroad["TRANSFER_SDATE"] = $edYear.'-04-01';
                $abroad["TRANSFER_EDATE"] = $row["END_DATE"];
                
                _update(SCHREG_TRANSFER_DAT, "", "", $abroad, $db, "2");
            }
        }
        //レコード作り終わったら一番最近のデータに単位数を入れる
        $query = knjo101Query::updateAbroadCredit($field["SCHREGNO"]);
        $db->query($query);
    }
    
    //DB切断
    Query::dbCheckIn($db);
    
    return 0;
}

?>
