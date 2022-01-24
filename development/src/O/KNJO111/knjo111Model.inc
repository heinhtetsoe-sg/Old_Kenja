<?php
class knjo111Model extends Model {
    var $grade;
    var $cmd;
    var $examyear;
    var $recordTableDiv;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限


    function knjo111Model()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->valErrMess = array();
        $this->valErrFlg = "";


        $this->cmd   = VARS::request("cmd");
        $this->file = VARS::file("XML");
        $this->radio = "2";
        $this->field["SCHREGNO"] = VARS::post("SCHREGNO");
        
        //パーツ作成用変数
        $this->partsCnt = 1;
        $this->parts = array();
        //学籍番号
        $this->parts[0] = array("TYPE"      =>  "text",
                                "NAME"      =>  "SCHREGNO",
                                "SIZE"      =>  10,
                                "MAXLENGTH" =>  8,
                                "EXTRA"     => "",
                                "LABEL"     =>  "学籍番号");
                                
        
        //ラジオボタン用の変数
        /*$this->radioName = "XSD";
        $this->radioVal = array(2, 1);
        $this->radioExtra = array("id=\"XSD1\"", "id=\"XSD2\"");
        $this->radioLabel = array("学習者情報", "学校保健");
        $this->radioLabelId = array("XSD1", "XSD2");*/
        $this->radioName = "";
        
        //画面タイトル用
        $this->title = "学校保健情報";
    }

    function fileUpdate()
    {
        /*if($this->radio == ""){
            $this->setMessage("xmlの種類を選択してください");
            return false;
        }else if($this->radio == "1"){*/
            //$this->xsd =  DOCUMENTROOT ."/common/XSD/"."eduAK3101s-2016-01.xsd";
        //}else{
            $this->xsd =  DOCUMENTROOT ."/common/XSD/"."eduAK3201s-2016-01.xsd";
        //}
        
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
        
        
        //保健取り込んだテーブルすべて
        $delArray = array("MEDEXAM_DET_DAT",
                          "MEDEXAM_TOOTH_DAT",
                          "H_APP_HOKEN_FROM_MST",
                          "H_APP_HOKEN_MST",
                          "H_APP_HOKEN_TO_MST",
                          "H_APP_H_ALIAS_DAT",
                          "H_APP_H_DENTAL_DAT",
                          "H_APP_H_GRADE_DAT",
                          "H_APP_H_MEDICAL_DAT",
                          "H_APP_H_OTHERADDR_DAT"
                         );
        foreach($delArray as $val){
            $query = knjo111Query::delete($this->field["SCHREGNO"], $val);
            $db->query($query);
        }
        
        Query::dbCheckIn($db);
    }

    function updateModel()
    {
        //データを配列に
        $data = getReadModel($this);
        
        //データ更新function
        HokenUpdate($this->field["SCHREGNO"], $data);
        
        //$this->setMessage("更新しました。");
        
        //とりあえずecho
        //print_r($data);
        return true;
    }

    function insertModel()
    {
        //賢者用データ作成Model
        $error = kenjaInsert($this->field);
        if($error != 0){
            $this->setMessage("取込データのコード値に不備があるため、\\n保健データを作成できませんでした。");
        }else{
            $this->setMessage("更新しました。");
        }
        return true;
    }

    
}

//学校保健(H_APP_HOKEN_テーブル)の更新
function HokenUpdate($schregNo, $data) 
{
    $hokenTo = array();     //H_APP_HOKEN_TO_MST更新用
    $hokenFrom = array();   //H_APP_HOKEN_FROM_MST更新用
    $hokenMst = array();    //H_APP_HOKEM_MST更新用
    
    $hokenTo["REGISTERCD"] = STAFFCD;
    $hokenTo["UPDATED"] = "sysdate()";
    $hokenTo["SCHREGNO"] = $schregNo;
    
    $hokenFrom["REGISTERCD"] = STAFFCD;
    $hokenFrom["UPDATED"] = "sysdate()";
    $hokenFrom["SCHREGNO"] = $schregNo;
    
    $hokenMst["REGISTERCD"] = STAFFCD;
    $hokenMst["UPDATED"] = "sysdate()";
    $hokenMst["SCHREGNO"] = $schregNo;
    
    //変換用配列
    //教育委員会
    $Kyouiku = array("教育委員会ID"     => "EDU_ID",
                     "外字教育委員会名" => "EDU_GNAME",
                     "内字教育委員会名" => "EDU_NNAME",
                     "部課係"           => "EDU_SECTION",
                     "住所コード"       => "EDU_ADDRCD",
                     "外字住所"         => "EDU_GADDR",
                     "内字住所"         => "EDU_NADDR",
                     "外字方書"         => "EDU_GKATAGAKI",
                     "内字方書"         => "EDU_NKATAGAKI",
                     "郵便番号"         => "EDU_ZIPCD",
                     "電話番号"         => "EDU_TELNO",
                     "職"               => "EDU_SYOKU");
                     
    //学校(分校の住所除く)
    $School = array("学校ID"        => "SCHOOL_ID",
                    "外字学校名"    => "SCHOOL_GNAME",
                    "内字学校名"    => "SCHOOL_NNAME",
                    "住所コード"    => "SCHOOL_ADDRCD",
                    "外字住所"      => "SCHOOL_GADDR",
                    "内字住所"      => "SCHOOL_NADDR",
                    "外字方書"      => "SCHOOL_GKATAGAKI",
                    "内字方書"      => "SCHOOL_NKATAGAKI",
                    "郵便番号"      => "SCHOOL_ZIPCD",
                    "分校ID"        => "SCHOOL_BUN_ID",
                    "外字分校名"    => "SCHOOL_BUN_GNAME",
                    "内字分校名"    => "SCHOOL_BUN_NNAME",
                    "電話番号"      => "SCHOOL_TELNO",
                    "職"            => "SCHOOL_SYOKU",
                    "備考"          => "SCHOOL_BIKOU");
    //分校住所
    $Bunko = array("住所コード" => "SCHOOL_BUN_ADDRCD",
                   "外字住所"   => "SCHOOL_BUN_GADDR",
                   "内字住所"   => "SCHOOL_BUN_NADDR",
                   "外字方書"   => "SCHOOL_BUN_GKATAGAKI",
                   "内字方書"   => "SCHOOL_BUN_NKATAGAKI",
                   "郵便番号"   => "SCHOOL_BUN_ZIPCD");
                   
    
    //DB接続
    $db = Query::dbCheckOut();
    
    //利用業務ユニット・送信先・送信元・生徒健康診断票・送信日時で分岐
    foreach($data as $hKey => $hVal){
        if($hKey == "利用業務ユニット"){
            
            //更新はH_APP_HOKEN_TO_MST
            $hokenTo["GYOUMU_UNIT"] = $hVal;
            
        }else if($hKey == "送信先情報"){
            foreach($hVal as $hfKey => $hfVal){
                if($hfKey == "教育委員会"){
                    foreach($hfVal as $hsKey => $hsVal){
                        if($hsKey != "所在地"){
                            $hokenTo[$Kyouiku[$hsKey]] = $hsVal;
                        }else{
                            foreach($hsVal as $htKey => $htVal){
                                $hokenTo[$Kyouiku[$htKey]] = $htVal;
                            }
                        }
                    }
                }else{      //学校のとき
                    foreach($hfVal as $hsKey => $hsVal){
                        if($hsKey != "所在地" && $hsKey != "分校所在地"){
                            $hokenTo[$School[$hsKey]] = $hsVal;
                        }else if($hsKey == "所在地"){
                            foreach($hsVal as $htKey => $htVal){
                                $hokenTo[$School[$htKey]] = $htVal;
                            }
                        }else{  //分校所在地
                            foreach($hsVal as $htKey => $htVal){
                                $hokenTo[$Bunko[$htKey]] = $htVal;
                            }
                        }
                    }
                }
            }
            
        }else if($hKey == "送信元情報"){
            foreach($hVal as $hfKey => $hfVal){
                if($hfKey == "教育委員会"){
                    foreach($hfVal as $hsKey => $hsVal){
                        if($hsKey != "所在地"){
                            $hokenFrom[$Kyouiku[$hsKey]] = $hsVal;
                        }else{
                            foreach($hsVal as $htKey => $htVal){
                                $hokenFrom[$Kyouiku[$htKey]] = $htVal;
                            }
                        }
                    }
                }else{      //学校のとき
                    foreach($hfVal as $hsKey => $hsVal){
                        if($hsKey != "所在地" && $hsKey != "分校所在地"){
                            $hokenFrom[$School[$hsKey]] = $hsVal;
                        }else if($hsKey == "所在地"){
                            foreach($hsVal as $htKey => $htVal){
                                $hokenFrom[$School[$htKey]] = $htVal;
                            }
                        }else{  //分校所在地
                            foreach($hsVal as $htKey => $htVal){
                                $hokenFrom[$Bunko[$htKey]] = $htVal;
                            }
                        }
                    }
                }
            }
            
        }else if($hKey == "送信日時"){
            //更新テーブルはH_APP_HOKEN_MST
            //Tを半角スペースに変換
            $send = str_replace("T", " ", $hVal);
            //+から上だけ使う
            $send_date = explode("+", $send);
            
            $hokenFrom["SEND_DATE"] = "'".$send_date[0].".000000'";
            
        }else{      //生徒健康診断票情報
            foreach($hVal as $hfKey => $hfVal){
                if($hfKey == "健康診断種別"){
                    
                    $hokenMst["HOKEN_SYUBETU"] = $hfVal;
                    
                }else if($hfKey == "生徒学生"){
                    $Student = array("生徒ID"       => "STUDENT_ID",
                                     "外字氏名"     => "STUDENT_GNAME",
                                     "内字氏名"     => "STUDENT_NNAME",
                                     "フリガナ"     => "STUDENT_KANA",
                                     "性別"         => "STUDENT_SEX",
                                     "生年月日"     => "STUDENT_BIRTHDAY",
                                     "住所コード"   => "STUDENT_ADDRCD",
                                     "外字住所"     => "STUDENT_GADDR",
                                     "内字住所"     => "STUDENT_NADDR",
                                     "外字方書"     => "STUDENT_GKATAGAKI",
                                     "内字方書"     => "STUDENT_NKATAGAKI",
                                     "郵便番号"     => "STUDENT_ZIPCD",
                                     "備考"         => "STUDENT_BIKOU",
                                     "国籍"         => "STUDENT_NATION");
                                     
                    
                    foreach($hfVal as $htKey => $htVal){
                        if($htKey == "基本情報"){
                            foreach($htVal as $hrKey => $hrVal){
                                if($hrKey != "通称名" && $hrKey != "現住所_その他"){
                                    if(is_array($hrVal)){
                                        foreach($hrVal as $hvKey => $hvVal){
                                            if(is_array($hvVal)){   //現住所
                                                foreach($hvVal as $hxKey => $hxVal){
                                                    $hokenMst[$Student[$hxKey]] = $hxVal;
                                                }
                                            }else{  //氏名
                                                $hokenMst[$Student[$hvKey]] = $hvVal;
                                            }
                                        }
                                    }else{
                                        $hokenMst[$Student[$hrKey]] = $hrVal;
                                    }
                                    
                                }else if($hrKey == "通称名"){
                                    //変換用
                                    $aliasChange = array("外字氏名" => "GNAME",
                                                         "内字氏名" => "NNAME",
                                                         "フリガナ" => "NAME_KANA");
                                    $aliasCnt = 1;
                                    $alias = array();
                                    $alias["REGISTERCD"] = STAFFCD;
                                    $alias["UPDATED"] = "sysdate()";
                                    $alias["SCHREGNO"] = $schregNo;
                                    
                                    foreach($hrVal as $hvKey => $hvVal){
                                        if(is_numeric($hvKey)){
                                            $aliasUpdate = 0;
                                            
                                            $alias["DATA_ROW"] = $aliasCnt;
                                            
                                            foreach($hvVal as $hxKey => $hxVal){
                                                $alias[$aliasChange[$hxKey]] = $hxVal;
                                            }
                                            _update(H_APP_H_ALIAS_DAT, "", "", $alias, $db, "2");
                                            
                                            $aliasCnt++;
                                            
                                        }else{
                                            $aliasUpdate = 1;
                                            
                                            $alias[$aliasChange[$hvKey]] = $hvVal;
                                            
                                        }
                                    }
                                    if($aliasUpdate != 0){
                                        $alias["DATA_ROW"] = $aliasCnt;
                                        
                                        _update(H_APP_H_ALIAS_DAT, "", "", $alias, $db, "2");
                                    }
                                    
                                }else if($hrKey == "現住所_その他"){
                                    //変換用
                                    $addrChange = array("住所コード"    => "ADDRCD",
                                                        "外字住所"      => "GADDR",
                                                        "内字住所"      => "NADDR",
                                                        "外字方書"      => "GKATAGAKI",
                                                        "内字方書"      => "NKATAGAKI",
                                                        "郵便番号"      => "ZIPCD",
                                                        "備考"          => "BIKOU");
                                    
                                    $addrCnt = 1;
                                    $addr = array();
                                    $addr["REGISTERCD"] = STAFFCD;
                                    $addr["UPDATED"] = "sysdate()";
                                    $addr["SCHREGNO"] = $schregNo;
                                    
                                    foreach($hrVal as $hvKey => $hvVal){
                                        if(is_numeric($hvKey)){
                                            $addrUpdate = 0;
                                            
                                            $addr["DATA_ROW"] = $addrCnt;
                                            
                                            foreach($hvVal as $hxKey => $hxVal){
                                                if($hxKey == "備考"){
                                                    $addr[$addrChange[$hxKey]] = $hxVal;
                                                }else{
                                                    foreach($hxVal as $hnKey => $hnVal){
                                                        $addr[$addrChange[$hnKey]] = $hnVal;
                                                    }
                                                }
                                            }
                                            _update(H_APP_H_OTHERADDR_DAT, "", "", $addr, $db, "2");
                                            //クリア
                                            $addr["BIKOU"] = "";
                                            
                                            $addrCnt++;
                                            
                                        }else{
                                            $addrUpdate = 1;
                                            
                                            if($hvKey == "備考"){
                                                $addr[$addrChange[$hvKey]] = $hvVal;
                                            }else{
                                                foreach($hvVal as $hxKey => $hxVal){
                                                    $addr[$addrChange[$hxKey]] = $hxVal;
                                                }
                                            }
                                        }
                                    }
                                    if($addrUpdate != 0){
                                        $addr["DATA_ROW"] = $addrCnt;
                                        
                                        _update(H_APP_H_OTHERADDR_DAT, "", "", $addr, $db, "2");
                                    }
                                }
                                
                                
                            }
                            
                        }else if($htKey == "高等学校_特別支援高等部"){
                            foreach($htVal as $hrKey => $hrVal){
                                if(is_array($hrVal)){
                                    if($hrKey == "所在地"){
                                        foreach($hrVal as $hvKey => $hvVal){
                                            $hokenMst[$School[$hvKey]] = $hvVal;
                                        }
                                    }else{
                                        foreach($hrVal as $hvKey => $hvVal){
                                            $hokenMst[$Bunko[$hvKey]] = $hvVal;
                                        }
                                    }
                                }else{
                                    $hokenMst[$School[$hrKey]] = $hrVal;
                                }
                            }
                        }else if($htKey == "在学学年"){
                            //変換用
                            $sGradeChange = array("教育課程の区分"    => "KUBUN",
                                                  "学年"              => "GRADE",
                                                  "原級留置回数"      => "STAY_CNT",
                                                  "学級"              => "HR_CLASS",
                                                  "番号"              => "ATTENDNO");
                                            
                            $sGradeCnt = 1;
                            $sGrade = array();
                            $sGrade["REGISTERCD"] = STAFFCD;
                            $sGrade["UPDATED"] = "sysdate()";
                            $sGrade["SCHREGNO"] = $schregNo;
                            
                            foreach($htVal as $hrKey => $hrVal){
                                if(is_numeric($hrKey)){
                                    $sGradeUpdate = 0;
                                    
                                    $sGrade["DATA_ROW"] = $sGradeCnt;
                                    
                                    foreach($hrVal as $hvKey => $hvVal){
                                        $sGrade[$sGradeChange[$hvKey]] = $hvVal;
                                    }
                                    _update(H_APP_H_GRADE_DAT, "", "", $sGrade, $db, "2");
                                    //クリア
                                    $sGrade["STAY_CNT"] = "";
                                    
                                    $sGradeCnt++;
                                    
                                }else{
                                    $sGradeUpdate = 1;
                                    
                                    $sGrade[$sGradeChange[$hrKey]] = $hrVal;
                                }
                            }
                            if($sGradeUpdate != 0){
                                $sGrade["DATA_ROW"] = $sGradeCnt;
                                
                                _update(H_APP_H_GRADE_DAT, "", "", $sGrade, $db, "2");
                            }
                        }
                    }
                }else if($hfKey == "健康診断一般"){
                    //変換用
                    $medChange = array("年度"               => "YEAR",
                                       "年齢"               => "AGE",
                                       "身長"               => "HEIGHT",
                                       "体重"               => "WEIGHT",
                                       "座高"               => "SIT_HEIGHT",
                                       "異常の有無"         => "_DISORDER",
                                       "外字疾病名"         => "_DISEASE_GNAME",
                                       "内字疾病名"         => "_DISEASE_NNAME",
                                       "右"                 => "_EYESIGHT_RIGHT",
                                       "左"                 => "_EYESIGHT_LEFT",
                                       "結果"               => "HEAR_RESULT_",
                                       "聴力レベル"         => "HEAR_LEVEL_",
                                       "間接撮影撮影日"     => "_FILM_TAKEDATE",
                                       "フィルム番号"       => "_FILM_NO",
                                       "所見"               => "_SYOKEN",
                                       "その他の検査"       => "_OTHER",
                                       "指導区分"           => "_SHIDOU_KUBUN",
                                       "外字臨床医学的検査" => "_TEST_GNAME",
                                       "内字臨床医学的検査" => "_TEST_NNAME",
                                       "蛋白第1次"          => "_PROTEINE",
                                       "糖第1次"            => "_SUGER",
                                       "潜血第1次"          => "_BLOOD",
                                       "事後措置"           => "AFTERCARE",
                                       "備考"               => "BIKOU");
                    
                    $parts = array("栄養状態"               => "NUT",
                                   "脊柱"                   => "SPINE",
                                   "胸郭"                   => "CHEST",
                                   "四肢"                   => "LIMB",
                                   "眼の疾病及び異常"       => "EYE",
                                   "耳"                     => "EAR",
                                   "鼻"                     => "NOSE",
                                   "咽頭"                   => "THROAT",
                                   "皮膚疾患"               => "SKIN",
                                   "結核"                   => "TB",
                                   "心臓"                   => "HEART",
                                   "尿"                     => "URINE",
                                   "寄生虫卵"               => "PARA",
                                   "その他の疾病及び異常"   => "OTHER");
                                   
                    $mediCnt = 1;
                    $medical = array();
                    $medical["REGISTERCD"] = STAFFCD;
                    $medical["UPDATED"] = "sysdate()";
                    $medical["SCHREGNO"] = $schregNo;
                    
                    foreach($hfVal as $hsKey => $hsVal){
                        if(is_numeric($hsKey)){
                            $medicalUpdate = 0;

                            //今回だけ下ですべて消すのでここで入れなおす
                            $medical["REGISTERCD"] = STAFFCD;
                            $medical["UPDATED"] = "sysdate()";
                            $medical["SCHREGNO"] = $schregNo;
                            
                            $medical["DATA_ROW"] = $mediCnt;
                            
                            foreach($hsVal as $htKey => $htVal){
                                if(!is_array($htVal)){
                                    $medical[$medChange[$htKey]] = $htVal;
                                }else{
                                    if(array_key_exists($htKey, $parts)){
                                        $partName = $parts[$htKey];
                                        foreach($htVal as $hrKey => $hrVal){
                                            if(is_array($hrVal)){
                                                foreach($hrVal as $hvKey => $hvVal){
                                                    $medical[$partName.$medChange[$hvKey]] = $hvVal;
                                                }
                                            }else{
                                                $medical[$partName.$medChange[$hrKey]] = $hrVal;
                                            }
                                        }
                                    }else{
                                        if($htKey != "脊柱胸郭四肢" && $htKey != "耳鼻咽頭疾患" && $htKey != "学校医"){
                                            if(is_array($htVal)){
                                                foreach($htVal as $hrKey => $hrVal){
                                                    if(is_array($hrVal)){
                                                        if($hrKey == "裸眼"){
                                                            $topPart = "N";
                                                            $lastPart = "";
                                                        }else if($hrKey == "矯正"){
                                                            $topPart = "C";
                                                            $lastPart = "";
                                                        }else if($hrKey == "右"){
                                                            $topPart = "";
                                                            $lastPart = "RIGHT";
                                                        }else if($hrKey == "左"){
                                                            $topPart = "";
                                                            $lastPart = "LEFT";
                                                        }else{
                                                            $topPart = "";
                                                            $lastPart = "";
                                                        }
                                                        
                                                        foreach($hrVal as $hvKey => $hvVal){
                                                            $medical[$topPart.$medChange[$hvKey].$lastPart] = $hvVal;
                                                        }
                                                    }else{
                                                        $medical[$medChange[$hrKey]] = $hrVal;
                                                    }
                                                }
                                            }
                                        }else if($htKey == "学校医"){
                                            $schoolDoc = array("所見"   => "SCH_DOCTOR_SYOKEN",
                                                               "年月日" => "SCH_DOCTOR_DATE");
                                            
                                            foreach($htVal as $hrKey => $hrVal){
                                                $medical[$schoolDoc[$hrKey]] = $hrVal;
                                            }
                                        }else{
                                            if($htKey == "脊柱胸郭四肢"){
                                                //疾病及び異常の部分
                                                $firstPartName = "SCL";
                                            }else{
                                                $firstPartName = "ENT";
                                            }
                                            foreach($htVal as $hrKey => $hrVal){
                                                if(array_key_exists($hrKey, $parts)){
                                                    $partName = $parts[$hrKey];
                                                }else{
                                                    $partName = $firstPartName;
                                                }
                                                foreach($hrVal as $hvKey => $hvVal){
                                                    $medical[$partName.$medChange[$hvKey]] = $hvVal;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            _update(H_APP_H_MEDICAL_DAT, "", "", $medical, $db, "2");
                            //クリア(対象のみを消すのはめんどくさいので一度すべて消す。)
                            $medical = array();
                            
                            $mediCnt++;
                            
                        }else{
                            $medicalUpdate = 1;
                            
                            if(!is_array($hsVal)){
                                $medical[$medChange[$hsKey]] = $hsVal;
                            }else{
                                if(array_key_exists($hsKey, $parts)){
                                    $partName = $parts[$hsKey];
                                    foreach($hsVal as $htKey => $htVal){
                                        if(is_array($htVal)){
                                            foreach($htVal as $hrKey => $hrVal){
                                                $medical[$partName.$medChange[$hrKey]] = $hrVal;
                                            }
                                        }else{
                                            $medical[$partName.$medChange[$htKey]] = $htVal;
                                        }
                                    }
                                }else{
                                    if($hsKey != "脊柱胸郭四肢" && $hsKey != "耳鼻咽頭疾患" && $hsKey != "学校医"){
                                        if(is_array($hsVal)){
                                            foreach($hsVal as $htKey => $htVal){
                                                if(is_array($htVal)){
                                                    if($htKey == "裸眼"){
                                                        $topPart = "N";
                                                        $lastPart = "";
                                                    }else if($htKey == "矯正"){
                                                        $topPart = "C";
                                                        $lastPart = "";
                                                    }else if($htKey == "右"){
                                                        $topPart = "";
                                                        $lastPart = "RIGHT";
                                                    }else if($htKey == "左"){
                                                        $topPart = "";
                                                        $lastPart = "LEFT";
                                                    }else{
                                                        $topPart = "";
                                                        $lastPart = "";
                                                    }
                                                    
                                                    foreach($htVal as $hrKey => $hrVal){
                                                        $medical[$topPart.$medChange[$hrKey].$lastPart] = $hrVal;
                                                    }
                                                }else{
                                                    $medical[$medChange[$htKey]] = $htVal;
                                                }
                                            }
                                        }
                                    }else if($hsKey == "学校医"){
                                        $schoolDoc = array("所見"   => "SCH_DOCTOR_SYOKEN",
                                                           "年月日" => "SCH_DOCTOR_DATE");
                                        
                                        foreach($hsVal as $htKey => $htVal){
                                            $medical[$schoolDoc[$htKey]] = $htVal;
                                        }
                                    }else{
                                        if($hsKey == "脊柱胸郭四肢"){
                                            //疾病及び異常の部分
                                            $firstPartName = "SCL";
                                        }else{
                                            $firstPartName = "ENT";
                                        }
                                        foreach($hsVal as $htKey => $htVal){
                                            if(array_key_exists($htKey, $parts)){
                                                $partName = $parts[$htKey];
                                            }else{
                                                $partName = $firstPartName;
                                            }
                                            foreach($htVal as $hrKey => $hrVal){
                                                $medical[$partName.$medChange[$hrKey]] = $hrVal;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if($medicalUpdate != 0){
                        $medical["DATA_ROW"] = $mediCnt;
                        
                        _update(H_APP_H_MEDICAL_DAT, "", "", $medical, $db, "2");
                        
                    }
                                       
                }else if($hfKey == "歯科検査"){
                    //変換用
                    $dentChange = array("年度"          => "YEAR",
                                        "年齢"          => "AGE",
                                        "歯列咬合"      => "DENTITION_BITE",
                                        "顎関節"        => "CHIN_JOINT",
                                        "歯垢の状態"    => "PLAQUE",
                                        "歯肉の状態"    => "GUM",
                                        "現在歯の有無"  => "_EXIST",
                                        "歯の状態"      => "_STATE",
                                        "補助記号"      => "_SUBMARK",
                                        "現在歯数"      => "_NOW_CNT",
                                        "未処置歯数"    => "_NOTTREAT_CNT",
                                        "処置歯数"      => "_TREAT_CNT",
                                        "喪失歯数"      => "_LOST_CNT",
                                        "異常の有無"    => "OTHER_DISORDER",
                                        "外字疾病名"    => "OTHER_DISEASE_GNAME",
                                        "内字疾病名"    => "OTHER_DISEASE_NNAME",
                                        "所見"          => "SCH_DENTIST_SYOKEN",
                                        "年月日"        => "SCH_DENTIST_DATE",
                                        "事後措置"      => "AFTERCARE");
                    
                    $place = array("右上" => "RU",
                                   "右下" => "RD",
                                   "左上" => "LU",
                                   "左下" => "LD");
                    
                    $dentCnt = 1;
                    $dent = array();
                    $dent["REGISTERCD"] = STAFFCD;
                    $dent["UPDATED"] = "sysdate()";
                    $dent["SCHREGNO"] = $schregNo;
                    
                    foreach($hfVal as $hsKey => $hsVal){
                        if(is_numeric($hsKey)){
                            $dentUpdate = 0;
                            
                            //全部消してまわるのでここで入れなおす。
                            $dent["REGISTERCD"] = STAFFCD;
                            $dent["UPDATED"] = "sysdate()";
                            $dent["SCHREGNO"] = $schregNo;
                            
                            $dent["DATA_ROW"] = $dentCnt;
                            
                            foreach($hsVal as $htKey => $htVal){
                                if(!is_array($htVal)){
                                    $dent[$dentChange[$htKey]] = $htVal;
                                }else{
                                    if($htKey != "歯式乳歯" && $htKey != "歯式永久歯"){
                                        if($htKey == "乳歯の本数"){
                                            $topParts = "BT";
                                        }else if($htKey == "永久歯の本数"){
                                            $topParts = "PT";
                                        }else{
                                            $topParts = "";
                                        }
                                        
                                        foreach($htVal as $hrKey => $hrVal){
                                            $dent[$topParts.$dentChange[$hrKey]] = $hrVal;
                                        }
                                    }else if($htKey == "歯式乳歯"){
                                        $dental = "_BT_";
                                        
                                        $alpha = array("A", "B", "C", "D", "E");
                                        
                                        foreach($htVal as $hrKey => $hrVal){
                                            $typeCnt = 0;
                                            
                                            $topPlace = $place[$hrKey];
                                            
                                            foreach($hrVal as $hvKey => $hvVal){
                                                foreach($hvVal as $hxKey => $hxVal){
                                                    $dent[$topPlace.$dental.$alpha[$typeCnt].$dentChange[$hxKey]] = $hxVal;
                                                }
                                                $typeCnt++;
                                            }
                                        }
                                        
                                    }else if($htKey == "歯式永久歯"){
                                        $dental = "_PT_";
                                        
                                        foreach($htVal as $hrKey => $hrVal){
                                            $typeCnt = 1;
                                            
                                            $topPlace = $place[$hrKey];
                                            
                                            foreach($hrVal as $hvKey => $hvVal){
                                                foreach($hvVal as $hxKey => $hxVal){
                                                    $dent[$topPlace.$dental.$typeCnt.$dentChange[$hxKey]] = $hxVal;
                                                }
                                                $typeCnt++;
                                            }
                                        }
                                        
                                    }
                                }
                            }
                            
                            _update(H_APP_H_DENTAL_DAT, "", "", $dent, $db, "2");
                            //面倒なので全部クリアする
                            $dent = array();
                            
                            $dentCnt++;
                            
                        }else{
                            $dentUpdate = 1;
                            
                            if(!is_array($hsVal)){
                                $dent[$dentChange[$hsKey]] = $hsVal;
                            }else{
                                if($hsKey != "歯式乳歯" && $hsKey != "歯式永久歯"){
                                    if($hsKey == "乳歯の本数"){
                                        $topParts = "BT";
                                    }else if($hsKey == "永久歯の本数"){
                                        $topParts = "PT";
                                    }else{
                                        $topParts = "";
                                    }
                                    
                                    foreach($hsVal as $htKey => $htVal){
                                        $dent[$topParts.$dentChange[$htKey]] = $htVal;
                                    }
                                }else if($hsKey == "歯式乳歯"){
                                    $dental = "_BT_";
                                    
                                    $alpha = array("A", "B", "C", "D", "E");
                                    
                                    foreach($hsVal as $htKey => $htVal){
                                        $typeCnt = 0;
                                        
                                        $topPlace = $place[$htKey];
                                        
                                        foreach($htVal as $hrKey => $hrVal){
                                            foreach($hrVal as $hvKey => $hvVal){
                                                $dent[$topPlace.$dental.$alpha[$typeCnt].$dentChange[$hvKey]] = $hvVal;
                                            }
                                            $typeCnt++;
                                        }
                                    }
                                    
                                }else if($hsKey == "歯式永久歯"){
                                    $dental = "_PT_";
                                    
                                    foreach($hsVal as $htKey => $htVal){
                                        $typeCnt = 1;
                                        
                                        $topPlace = $place[$htKey];
                                        
                                        foreach($htVal as $hrKey => $hrVal){
                                            foreach($hrVal as $hvKey => $hvVal){
                                                $dent[$topPlace.$dental.$typeCnt.$dentChange[$hvKey]] = $hvVal;
                                            }
                                            $typeCnt++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if($dentUpdate != 0){
                        $dent["DATA_ROW"] = $dentCnt;
                        
                        _update(H_APP_H_DENTAL_DAT, "", "", $dent, $db, "2");
                    }
                }
            }
        }
    }
    
    
    //H_APP_HOKEN_TO_MST更新
    _update(H_APP_HOKEN_TO_MST, "", "", $hokenTo, $db, "2");
    
    //H_APP_HOKEN_FROM_MST更新
    _update(H_APP_HOKEN_FROM_MST, "", "", $hokenFrom, $db, "2");
    
    //H_APP_HOKEN_MST更新
    _update(H_APP_HOKEN_MST, "", "", $hokenMst, $db, "2");
    
    
    //DB切断
    Query::dbCheckIn($db);
}

function kenjaInsert($field)
{
    //DB接続
    $db = Query::dbCheckOut();
    
    //コードエラーチェック
    //区分と学年があっているか
    $query = knjo111Query::getKubunCheck($field["SCHREGNO"]);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if($row["KUBUN"] != "S2"){
            //学年制
            $check[0] = substr($row["GRADE"], 0, 1);
            $check[1] = substr($row["GRADE"], 1, 1);
            
            if($check[0] != "H"){
                //DB切断
                Query::dbCheckIn($db);
                return 1;   //エラー
            }else{
                if($check[1] < 1 || $check[1] > 3){
                    //DB切断
                    Query::dbCheckIn($db);
                    return 1;
                }
            }
        }else{
            //単位制
            $check[0] = substr($row["GRADE"], 0, 1);
            
            if(!preg_match("/^[0-9]+$/", $check[0])){
                //DB切断
                Query::dbCheckIn($db);
                return 1;   //エラー
            }else{
                $check[1] = number_format($row["GRADE"]);
                
                if($check[1] < 1 || $check[1] > 10){
                    //DB切断
                    Query::dbCheckIn($db);
                    return 1;
                }
            }
        }
    }
    
    
    //視力検査のコード配列
    $query = knjo111Query::getKenjaCode("02");
    $result = $db->query($query);
    $eyeSight = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $eyeSight[$row["DICTIONARY_CODE"]] = $row["KENJA_CODE"];
    }
    
    //聴力検査のコード配列
    $query = knjo111Query::getKenjaCode("03");
    $result = $db->query($query);
    $hearCode = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $hearCode[$row["DICTIONARY_CODE"]] = $row["KENJA_CODE"];
    }
    
    //MEDEXAM_DET_DAT用データ取得
    $query = knjo111Query::getMedexamData($field["SCHREGNO"], "1");
    $cnt = $db->getOne($query);
    if($cnt > 0){
        $query = knjo111Query::getMedexamData($field["SCHREGNO"]);
        $result = $db->query($query);
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $medexam = array();
            $medexam["REGISTERCD"] = STAFFCD;
            $medexam["UPDATED"] = "sysdate()";
            
            $medexam["SCHREGNO"] = $field["SCHREGNO"];
            
            $medexam["YEAR"] = $row["YEAR"];
            $medexam["HEIGHT"] = $row["HEIGHT"];
            $medexam["WEIGHT"] = $row["WEIGHT"];
            $medexam["SITHEIGHT"] = $row["SIT_HEIGHT"];
            
            //視力検査結果
            if($row["N_EYESIGHT_RIGHT"] != "" && array_key_exists($row["N_EYESIGHT_RIGHT"], $eyeSight)){
                $medexam["R_BAREVISION_MARK"] = $eyeSight[$row["N_EYESIGHT_RIGHT"]];
            }
            if($row["N_EYESIGHT_LEFT"] != "" && array_key_exists($row["N_EYESIGHT_LEFT"], $eyeSight)){
                $medexam["L_BAREVISION_MARK"] = $eyeSight[$row["N_EYESIGHT_LEFT"]];
            }
            if($row["C_EYESIGHT_RIGHT"] != "" && array_key_exists($row["C_EYESIGHT_RIGHT"], $eyeSight)){
                $medexam["R_VISION_MARK"] = $eyeSight[$row["C_EYESIGHT_RIGHT"]];
            }
            if($row["C_EYESIGHT_LEFT"] != "" && array_key_exists($row["C_EYESIGHT_LEFT"], $eyeSight)){
                $medexam["L_VISION_MARK"] = $eyeSight[$row["C_EYESIGHT_LEFT"]];
            }
            
            //聴力検査結果
            if($row["HEAR_RESULT_RIGHT"] != "" && array_key_exists($row["HEAR_RESULT_RIGHT"], $hearCode)){
                $medexam["R_EAR"] = $hearCode[$row["HEAR_RESULT_RIGHT"]];
            }
            $medexam["R_EAR_DB"] = $row["HEAR_LEVEL_RIGHT"];
            
            if($row["HEAR_RESULT_LEFT"] != "" && array_key_exists($row["HEAR_RESULT_LEFT"], $hearCode)){
                $medexam["L_EAR"] = $hearCode[$row["HEAR_RESULT_LEFT"]];
            }
            $medexam["L_EAR_DB"] = $row["HEAR_LEVEL_LEFT"];
            
            _update(MEDEXAM_DET_DAT, "", "", $medexam, $db, "2");
        }
    }
    
    //MEDEXAM_TOOTH_DAT作成
    //歯列咬合と顎関節
    $query = knjo111Query::getKenjaCode("07");
    $result = $db->query($query);
    $joint = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $joint[$row["DICTIONARY_CODE"]] = $row["KENJA_CODE"];
    }
    //歯垢の状態
    $query = knjo111Query::getKenjaCode("08");
    $result = $db->query($query);
    $plaque = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $plaque[$row["DICTIONARY_CODE"]] = $row["KENJA_CODE"];
    }
    //歯肉の状態
    $query = knjo111Query::getKenjaCode("09");
    $result = $db->query($query);
    $gum = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $gum[$row["DICTIONARY_CODE"]] = $row["KENJA_CODE"];
    }
    //歯の状態
    $query = knjo111Query::getKenjaCode("11");
    $result = $db->query($query);
    $state = array();
    $before = "";
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if($before != $row["DICTIONARY_CODE"]){
            $state[$row["DICTIONARY_CODE"]] = $row["KENJA_CODE"];
        }
        $before = $row["DICTIONARY_CODE"];
    }
    //補助記号
    $query = knjo111Query::getKenjaCode("12");
    $result = $db->query($query);
    $submark = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $submark[$row["DICTIONARY_CODE"]] = $row["KENJA_CODE"];
    }
    
    //歯科検査用配列
    //乳歯か永久歯か
    $kToothType = array("BABY","ADULT");
    $aToothType = array("BT", "PT");
    
    //場所
    $kPlace = array("UP_R", "UP_L", "LW_R", "LW_L");
    $aPlace = array("RU", "LU", "RD", "LD");
    
    //H_APP_H_DENTAL_DATの場合の乳歯のときのみ使用
    $btPlace = array("A", "B", "C", "D", "E");
    
    //データ取得
    $query = knjo111Query::getToothData($field["SCHREGNO"], "1");
    $cnt = $db->getOne($query);
    if($cnt > 0){
        $query = knjo111Query::getToothData($field["SCHREGNO"]);
        $result = $db->query($query);
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $tooth = array();
            $tooth["REGISTERCD"] = STAFFCD;
            $tooth["UPDATED"] = "sysdate()";
            
            $tooth["SCHREGNO"] = $field["SCHREGNO"];
            
            $tooth["YEAR"] = $row["YEAR"];
            if($row["DENTITION_BITE"] != ""){
                $tooth["JAWS_JOINTCD"] = $joint[$row["DENTITION_BITE"]];
            }
            if($row["CHIN_JOINT"] != ""){
                $tooth["JAWS_JOINTCD2"] = $joint[$row["CHIN_JOINT"]];
            }
            if($row["PLAQUE"] != ""){
                $tooth["PLAQUECD"] = $plaque[$row["PLAQUE"]];
            }
            if($row["GUM"] != ""){
                $tooth["GUMCD"] = $gum[$row["GUM"]];
            }
            
            //歯式まわして作成
            for($i=0;$i<2;$i++){
                //$i=0は乳歯、$i=1は永久歯
                if($i>0){
                    $count = 8; //永久歯は8回
                }else{
                    $count = 5; //乳歯は5回
                }
                $ktype = $kToothType[$i];
                $atype = $aToothType[$i];
                for($j=0;$j<4;$j++){
                    //$j=0は右上,$j=1は左上,$j=2は右下,$j=3は左下
                    $kplace = $kPlace[$j];
                    $aplace = $aPlace[$j];
                    for($k=0;$k<$count;$k++){
                        if($i==0){
                            $anumber = $btPlace[$k];
                            $knumber = $k+1;
                        }else{
                            $anumber = $k+1;
                            $knumber = $k+1;
                        }

                        if($row[$aplace."_".$atype."_".$anumber."_EXIST"] != "" && $row[$aplace."_".$atype."_".$anumber."_EXIST"] != "2"){
                            if($row[$aplace."_".$atype."_".$anumber."_SUBMARK"] != "" && $row[$aplace."_".$atype."_".$anumber."_STATE"] == "0"){
                                $tooth[$kplace."_".$ktype.$knumber] = $submark[$row[$aplace."_".$atype."_".$anumber."_SUBMARK"]];
                            }else{
                                $tooth[$kplace."_".$ktype.$knumber] = $state[$row[$aplace."_".$atype."_".$anumber."_STATE"]];
                            }
                        }
                        
                    }
                }
            }
            
            //乳歯本数
            if($row["BT_NOW_CNT"] != ""){
                $tooth["BABYTOOTH"] = $row["BT_NOW_CNT"];
            }else{
                $tooth["BABYTOOTH"] = 0;
            }
            if($row["BT_NOTTREAT_CNT"] != ""){
                $tooth["REMAINBABYTOOTH"] = $row["BT_NOTTREAT_CNT"];
            }else{
                $tooth["REMAINBABYTOOTH"] = 0;
            }
            if($row["BT_TREAT_CNT"] != ""){
                $tooth["TREATEDBABYTOOTH"] = $row["BT_TREAT_CNT"];
            }else{
                $tooth["TREATEDBABYTOOTH"] = 0;
            }
            //永久歯本数
            if($row["PT_NOW_CNT"] != ""){
                $tooth["ADULTTOOTH"] = $row["PT_NOW_CNT"];
            }else{
                $tooth["ADULTTOOTH"] = 0;
            }
            if($row["PT_NOTTREAT_CNT"] != ""){
                $tooth["REMAINADULTTOOTH"] = $row["PT_NOTTREAT_CNT"];
            }else{
                $tooth["REMAINADULTTOOTH"] = 0;
            }
            if($row["PT_TREAT_CNT"] != ""){
                $tooth["TREATEDADULTTOOTH"] = $row["PT_TREAT_CNT"];
            }else{
                $tooth["TREATEDADULTTOOTH"] = 0;
            }
            if($row["PT_LOST_CNT"] != ""){
                $tooth["LOSTADULTTOOTH"] = $row["PT_LOST_CNT"];
            }else{
                $tooth["LOSTADULTTOOTH"] = 0;
            }
            
            //その他の疾病及び異常
            if($row["OTHER_DISEASE_DISORDER"] != "" && $row["OTHER_DISEASE_DISORDER"] != "0"){
                $tooth["OTHERDISEASECD"] = "99";    //その他
                if($row["OTHER_DISEASE_NNAME"] != ""){
                    $tooth["OTHERDISEASE"] = $row["OTHER_DISEASE_NNAME"];
                }
            }
            
            //学校歯科医
            if($row["SCH_DENTIST_SYOKEN"] != ""){
                $tooth["DENTISTREMARK"] = $row["SCH_DENTIST_SYOKEN"];
            }
            if($row["SCH_DENTIST_DATE"] != ""){
                $tooth["DENTISTREMARKDATE"] = $row["SCH_DENTIST_DATE"];
            }
            //事後措置
            if($row["AFTERCARE"] != ""){
                $tooth["DENTISTTREAT"] = $row["AFTERCARE"];
            }
            
            _update(MEDEXAM_TOOTH_DAT, "", "", $tooth, $db, "2");
        }
    }

    Query::dbCheckIn($db);
    return 0;

}
    
?>
