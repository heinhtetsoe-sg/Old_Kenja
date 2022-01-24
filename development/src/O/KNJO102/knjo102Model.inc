<?php
//require_once('dBug.php');

class knjo102Model extends Model {



    var $grade;
    var $cmd;
    var $examyear;
    var $recordTableDiv;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限


    function knjo102Model()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->valErrMess = array();
        $this->valErrFlg = "";
        $this->new = array();

        $this->cmd   = VARS::request("cmd");
        $this->SCHREGNO = VARS::post("SCHREGNO");
        $this->SEND = VARS::post("SEND") != "" ? VARS::post("SEND") : "1";
        
        $this->field = array("K_ID"         => VARS::post("K_ID"),
                             
                             "K_NAME"       => VARS::post("K_NAME"),
                             
                             "K_BKK"        => VARS::post("K_BKK"),
                             
                             "K_ADDRCD"     => VARS::post("K_ADDRCD"),
                             
                             "K_ZIPCD"      => VARS::post("K_ZIPCD"),
                             
                             "K_ADDRESS"    => VARS::post("K_ADDRESS"),
                             
                             "K_KATAGAKI"   => VARS::post("K_KATAGAKI"),
                             
                             "K_TELNO"      => VARS::post("K_TELNO"),
                             
                             "K_SYOKU"      => VARS::post("K_SYOKU"),
                             
                             "G_ID"         => VARS::post("G_ID"),
                             
                             "G_NAME"       => VARS::post("G_NAME"),
                             
                             "G_ADDRCD"     => VARS::post("G_ADDRCD"),
                             
                             "G_ZIPCD"      => VARS::post("G_ZIPCD"),
                             
                             "G_ADDRESS"    => VARS::post("G_ADDRESS"),
                             
                             "G_KATAGAKI"   => VARS::post("G_KATAGAKI"),
                             
                             "G_TELNO"      => VARS::post("G_TELNO"),
                             
                             "G_SYOKU"      => VARS::post("G_SYOKU"),
                             
                             "G_BIKOU"      => VARS::post("G_BIKOU"),
                             
                             "G_BUNID"         => VARS::post("G_BUNID"),
                             
                             "G_BUNNAME"       => VARS::post("G_BUNNAME"),
                             
                             "G_BUNADDRCD"     => VARS::post("G_BUNADDRCD"),
                             
                             "G_BUNZIPCD"      => VARS::post("G_BUNZIPCD"),
                             
                             "G_BUNADDRESS"    => VARS::post("G_BUNADDRESS"),
                             
                             "G_BUNKATAGAKI"   => VARS::post("G_BUNKATAGAKI"),
                             
                            );
    }

    function updateModel()
    {
        $flg = false;
        
        //プロパティファイル読み込み
        $this->getProperties();

        //DB接続
        $db = Query::dbCheckOut();

        
        $data = array();
        
        //エラーチェック
        $query = knjo102Query::getCheck($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if($row["CNT"] == ""){
            $this->setMessage("対象のデータはありません。");
            return false;
        }else{
            if($row["SCHOOL_KIND"] != "H"){  //SCHOOL_KINDが高校以外だったらエラーにする
                $this->setMessage("年度対象外です。");
                return false;
            }
        }
        //データ作成
        $data["利用業務ユニット"] = "AK31";
        
        //送信先
        if($this->SEND == "1"){
            $data["送信先情報"] = "";
        }else{
            if($this->SEND != "3"){
                //教育委員会
                if($this->field["K_ID"] != "" 
                   || $this->field["K_NAME"] != "" 
                   || $this->field["K_BKK"] != "" 
                   || $this->field["K_ADDRCD"] != "" 
                   || $this->field["K_ADDRESS"] != "" 
                   || $this->field["K_KATAGAKI"] != "" 
                   || $this->field["K_ZIPCD"] != "" 
                   || $this->field["K_TELNO"] != "" 
                   || $this->field["K_SYOKU"] != "")
                {
                    if($this->field["K_ID"] != ""){
                        $data["送信先情報"]["教育委員会"]["教育委員会ID"] = $this->field["K_ID"];
                    }
                    $data["送信先情報"]["教育委員会"]["外字教育委員会名"] = "";
                    $data["送信先情報"]["教育委員会"]["内字教育委員会名"] = $this->field["K_NAME"];
                    if($this->field["K_BBK"] != ""){
                        $data["送信先情報"]["教育委員会"]["部課係"] = $this->field["K_BKK"];
                    }
                    if($this->field["K_ADDRCD"] != "" || $this->field["K_ADDRESS"] != "" || $this->field["K_KATAGAKI"] != "" || $this->field["K_ZIPCD"] != ""){
                        $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:住所コード"] = $this->field["K_ADDRCD"];
                        $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:外字住所"] = "";
                        $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:内字住所"] = $this->field["K_ADDRESS"];
                        $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:外字方書"] = "";
                        $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:内字方書"] = $this->field["K_KATAGAKI"];
                        $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:郵便番号"] = $this->field["K_ZIPCD"];
                    }
                    if($this->field["K_TELNO"] != ""){
                        $data["送信先情報"]["教育委員会"]["電話番号"] = $this->field["K_TELNO"];
                    }
                    if($this->field["K_SYOKU"] != ""){
                        $data["送信先情報"]["教育委員会"]["職"] = $this->field["K_SYOKU"];
                    }
                }
            }
            if($this->SEND != "2"){
                //学校
                if($this->field["G_ID"] != "" || 
                   $this->field["G_NAME"] != "" || 
                   $this->field["G_BKK"] != "" || 
                   $this->field["G_ADDRCD"] != "" || 
                   $this->field["G_ADDRESS"] != "" || 
                   $this->field["G_KATAGAKI"] != "" || 
                   $this->field["G_ZIPCD"] != "" || 
                   $this->field["G_TELNO"] != "" || 
                   $this->field["G_BUNID"] != "" || 
                   $this->field["G_BUNNAME"] != "" || 
                   $this->field["G_BUNADDRCD"] != "" || 
                   $this->field["G_BUNADDRESS"] != "" || 
                   $this->field["G_BUNKATAGAKI"] != "" || 
                   $this->field["G_BUNZIPCD"] != "" || 
                   $this->field["G_TELNO"] != "" || 
                   $this->field["G_SYOKU"] != "" || 
                   $this->field["G_BIKOU"] != "")
                {
                    if($this->field["G_ID"] != ""){
                        $data["送信先情報"]["学校"]["学校ID"] = $this->field["G_ID"];
                    }
                    $data["送信先情報"]["学校"]["外字学校名"] = "";
                    $data["送信先情報"]["学校"]["内字学校名"] = $this->field["G_NAME"];
                    $data["送信先情報"]["学校"]["所在地"]["lgxml:住所コード"] = $this->field["G_ADDRCD"];
                    $data["送信先情報"]["学校"]["所在地"]["lgxml:外字住所"] = "";
                    $data["送信先情報"]["学校"]["所在地"]["lgxml:内字住所"] = $this->field["G_ADDRESS"];
                    $data["送信先情報"]["学校"]["所在地"]["lgxml:外字方書"] = "";
                    $data["送信先情報"]["学校"]["所在地"]["lgxml:内字方書"] = $this->field["G_KATAGAKI"];
                    $data["送信先情報"]["学校"]["所在地"]["lgxml:郵便番号"] = $this->field["G_ZIPCD"];
                    if($this->field["G_BUNID"] != ""){
                        $data["送信先情報"]["学校"]["分校ID"] = $this->field["G_BUNID"];
                    }
                    if($this->field["G_BUNNAME"] != ""){
                        $data["送信先情報"]["学校"]["外字分校名"] = "";
                        $data["送信先情報"]["学校"]["内字分校名"] = $this->field["G_BUNNAME"];
                    }
                    if($this->field["G_BUNADDRCD"] != "" || $this->field["G_BUNADDRESS"] != "" || $this->field["G_BUNKATAGAKI"] != "" || $this->field["G_BUNZIPCD"] != ""){
                        $data["送信先情報"]["学校"]["分校所在地"]["lgxml:住所コード"] = $this->field["G_BUNADDRCD"];
                        $data["送信先情報"]["学校"]["分校所在地"]["lgxml:外字住所"] = "";
                        $data["送信先情報"]["学校"]["分校所在地"]["lgxml:内字住所"] = $this->field["G_BUNADDRESS"];
                        $data["送信先情報"]["学校"]["分校所在地"]["lgxml:外字方書"] = "";
                        $data["送信先情報"]["学校"]["分校所在地"]["lgxml:内字方書"] = $this->field["G_BUNKATAGAKI"];
                        $data["送信先情報"]["学校"]["分校所在地"]["lgxml:郵便番号"] = $this->field["G_BUNZIPCD"];
                    }
                    if($this->field["G_TELNO"] != ""){
                        $data["送信先情報"]["学校"]["電話番号"] = $this->field["G_TELNO"];
                    }
                    if($this->field["G_SYOKU"] != ""){
                        $data["送信先情報"]["学校"]["職"] = $this->field["G_SYOKU"];
                    }
                    if($this->field["G_BIKOU"] != ""){
                        $data["送信先情報"]["学校"]["備考"] = $this->field["G_BIKOU"];
                    }
                }
            }
        }
        
        //送信元
        $query = knjo102Query::getMotoSchool();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        $data["送信元情報"]["学校"]["外字学校名"] = "";
        $data["送信元情報"]["学校"]["内字学校名"] = $row["SCHOOLNAME1"];
        $data["送信元情報"]["学校"]["所在地"]["lgxml:住所コード"] = "";
        $data["送信元情報"]["学校"]["所在地"]["lgxml:外字住所"] = "";
        $data["送信元情報"]["学校"]["所在地"]["lgxml:内字住所"] = $row["SCHOOLADDR1"];
        $data["送信元情報"]["学校"]["所在地"]["lgxml:外字方書"] = "";
        $data["送信元情報"]["学校"]["所在地"]["lgxml:内字方書"] = $row["SCHOOLADDR2"];
        $data["送信元情報"]["学校"]["所在地"]["lgxml:郵便番号"] = $row["SCHOOLZIPCD"];
        $data["送信元情報"]["学校"]["電話番号"] = $row["SCHOOLTELNO"];
        
        //出力ファイル名用に保持
        $this->schoolName = $row["SCHOOLNAME1"];
        
        //指導要録情報　SCHREG_BASE_MSTから取得
        $query = knjo102Query::getHandicap($this->SCHREGNO);
        $handicap = $db->getOne($query);
        if($handicap == "002"){     //身体障害者・病弱者高等部判断用フラグ
            $data["指導要録情報"]["指導要録種別"] = "K02";
            $this->SYUBETU = 1;
        }else if($handicap == "003"){  //知的障害者高等部
            $data["指導要録情報"]["指導要録種別"] = "K03";
            $this->SYUBETU = 2;
        }else{
            $data["指導要録情報"]["指導要録種別"] = "K01";
            $this->SYUBETU = 0;
        }
        
        //指導要録情報＞学校変更状況
        //学校変更状況＞年度・教育課程の区分
        $Query = knjo102Query::getChangeYear($this->SCHREGNO);
        $Result = $db->query($Query);
        
        $changeCnt = 0;
        while($Row = $Result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["指導要録情報"]["学校変更状況"][$changeCnt]["年度"] = $Row["YEAR"];
            $data["指導要録情報"]["学校変更状況"][$changeCnt]["教育課程の区分"] = $Row["DICTIONARY_CODE"];
            if($changeCnt == 0){
                //入学時年度の学年制か単位制のフラグ
                $this->schoolDiv = $Row["SCHOOLDIV"];
            }
            
            //学校変更状況＞学年・原級留置回数
            $Query = knjo102Query::getChangeGrade($this->SCHREGNO, $Row["YEAR"]);
            $row = $db->getRow($Query, DB_FETCHMODE_ASSOC);
            if($this->schoolDiv != 0){
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["学年"] = $row["ANNUAL"];
            }else{
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["学年"] = $row["GRADE"];
            }
            if($row["COUNT"] > 1){
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["原級留置回数"] = $row["COUNT"];
            }
            
            //学校変更状況＞校長氏名
            //STAFF_PRINCIPAL_HIST_DATにデータがあるかをカウントする
            $query = knjo102Query::getPrincipal($Row["YEAR"], "1");
            $cnt = $db->getOne($query);
            if($cnt > 0){
                //データをPRINCIPAL_HIST_DATに取りに行く
                $query = knjo102Query::getPrincipal($Row["YEAR"]);
                $result = $db->query($query);
                
                $count = 0;
                
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"][$count]["氏名"]["lgxml:外字氏名"] = "";
                    $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"][$count]["氏名"]["lgxml:内字氏名"] = $row["STAFFNAME"];
                    $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"][$count]["氏名"]["lgxml:フリガナ"] = $row["STAFFNAME_KANA"];
                    
                    $count++;
                }
                if($count == 0){
                    //0のままのとき(データがなかったとき)
                    $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"][$count]["氏名"]["lgxml:外字氏名"] = "";
                    $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"][$count]["氏名"]["lgxml:内字氏名"] = "";
                    $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"][$count]["氏名"]["lgxml:フリガナ"] = "";
                }
                
            }else{
                //データをCERTIF_SCHOOL_DATから取得
                $query = knjo102Query::getCertif($Row["YEAR"]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"]["氏名"]["lgxml:外字氏名"] = "";
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"]["氏名"]["lgxml:内字氏名"] = $row["PRINCIPAL_NAME"];
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"]["氏名"]["lgxml:フリガナ"] = "";
            }
            
            //学校変更状況＞ホームルーム担任氏名
            //カウント
            $query = knjo102Query::getHRteacher($this->SCHREGNO, $Row["YEAR"]);
            $result = $db->query($query);
            
            $count = 0;
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["ホームルーム担任者氏名"][$count]["氏名"]["lgxml:外字氏名"] = "";
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["ホームルーム担任者氏名"][$count]["氏名"]["lgxml:内字氏名"] = $row["STAFFNAME"];
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["ホームルーム担任者氏名"][$count]["氏名"]["lgxml:フリガナ"] = $row["STAFFNAME_KANA"];
                
                $count++;
            }
            if($count == 0){
                //0のままのとき(データがなかったとき)
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["ホームルーム担任者氏名"][$count]["氏名"]["lgxml:外字氏名"] = "";
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["ホームルーム担任者氏名"][$count]["氏名"]["lgxml:内字氏名"] = "";
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["ホームルーム担任者氏名"][$count]["氏名"]["lgxml:フリガナ"] = "";
            }
            $changeCnt++;
        }
        
        //指導要録情報＞学籍の記録
        //学籍の記録＞生徒＞氏名・通称名・性別・生年月日・現住所・国籍
        $query = knjo102Query::getSchregData($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        $data["指導要録情報"]["学籍の記録"]["生徒"]["氏名"]["lgxml:外字氏名"] = "";
        $data["指導要録情報"]["学籍の記録"]["生徒"]["氏名"]["lgxml:内字氏名"] = $row["NAME"];
        $data["指導要録情報"]["学籍の記録"]["生徒"]["氏名"]["lgxml:フリガナ"] = $row["NAME_KANA"];
        $data["指導要録情報"]["学籍の記録"]["生徒"]["通称名"]["lgxml:外字氏名"] = "";
        $data["指導要録情報"]["学籍の記録"]["生徒"]["通称名"]["lgxml:内字氏名"] = $row["NAME_SHOW"];
        $data["指導要録情報"]["学籍の記録"]["生徒"]["通称名"]["lgxml:フリガナ"] = $row["NAME_KANA"];
        $data["指導要録情報"]["学籍の記録"]["生徒"]["性別"] = $row["SEX"];
        $data["指導要録情報"]["学籍の記録"]["生徒"]["生年月日"] = $row["BIRTHDAY"];
        $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:住所コード"] = "";
        $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:外字住所"] = "";
        $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:内字住所"] = $row["ADDR1"];
        $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:外字方書"] = "";
        if($row["ADDR_FLG"] == '1'){
            $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:内字方書"] = $row["ADDR2"];
        }else{
            $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:内字方書"] = "";
        }
        $data["指導要録情報"]["学籍の記録"]["生徒"]["現住所"]["住所"]["lgxml:郵便番号"] = $row["ZIPCD"];
        if($row["NATION"] != ""){
            $data["指導要録情報"]["学籍の記録"]["生徒"]["国籍"] = $row["NATION"];
        }
        
        //学籍の記録＞生徒＞保護者
        $query = knjo102Query::getGuardianData($this->SCHREGNO);
        $result = $db->query($query);
        
        $count = 0;
        
        while($row = $result->fetchRow(SB_FETCHMODE_ASSOC)){
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["氏名"]["lgxml:外字氏名"] = "";
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["氏名"]["lgxml:内字氏名"] = $row["GUARD_NAME"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["氏名"]["lgxml:フリガナ"] = $row["GUARD_KANA"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:住所コード"] = "";
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:外字住所"] = "";
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:内字住所"] = $row["GUARD_ADDR1"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:外字方書"] = "";
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:内字方書"] = $row["GUARD_ADDR2"];
            $data["指導要録情報"]["学籍の記録"]["生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:郵便番号"] = $row["GUARD_ZIPCD"];
            
            $count++;
        }
        
        
        
        //学籍の記録＞在学状況＞学校情報(送信元情報と同じでOK)
        $query = knjo102Query::getMotoSchool();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["外字学校名"] = "";
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["内字学校名"] = $row["SCHOOLNAME1"];
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:住所コード"] = "";
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:外字住所"] = "";
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:内字住所"] = $row["SCHOOLADDR1"];
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:外字方書"] = "";
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:内字方書"] = $row["SCHOOLADDR2"];
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:郵便番号"] = $row["SCHOOLZIPCD"];
        $data["指導要録情報"]["学籍の記録"]["在学状況"]["学校情報"]["電話番号"] = $row["SCHOOLTELNO"];
        
        //学籍の記録＞在学状況＞在学学年
        $query = knjo102Query::getZaigakuGrade($this->SCHREGNO);
        $result = $db->query($query);
        
        $count = 0;
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["年度"] = $row["YEAR"];
            if($this->schoolDiv != 0){
                $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["学年または年次"] = $row["ANNUAL"];
            }else{
                $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["学年または年次"] = $row["GRADE"];
            }
            if($this->properties["useSchregRegdHdat"] != 0){
                if($row["HR_CLASS_NAME1"] != ""){
                    $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["ホームルーム"] = $row["HR_CLASS_NAME1"];
                }else{
                    $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["ホームルーム"] = $row["HR_CLASS"];
                }
            }else{
                if($row["NAME1"] != ""){
                    $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["ホームルーム"] = $row["NAME1"];
                }else{
                    $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["ホームルーム"] = $row["HR_CLASS"];
                }
            }
            $data["指導要録情報"]["学籍の記録"]["在学状況"]["在学学年"][$count]["整理番号"] = $row["ATTENDNO"];
            
            $count++;
        }
        
        //学籍の記録＞異動状況＞入学or編入学or転入学・転学or退学or卒業
        $query = knjo102Query::getIdou($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        if($row["ENT_DIV"] == "4"){
            //転入学
            $data["指導要録情報"]["学籍の記録"]["異動状況"]["転入学"]["転入学年月日"] = $row["ENT_DATE"];
            if($this->schoolDiv != 0){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["転入学"]["転入学時学年"] = $row["ANNUAL"];
            }else{
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["転入学"]["転入学時学年"] = $row["GRADE"];
            }
            if($row["ENT_REASON"] != ""){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["転入学"]["備考"] = $row["ENT_REASON"];
            }
        }else if($row["ENT_DIV"] == "5"){
            //編入学
            $data["指導要録情報"]["学籍の記録"]["異動状況"]["編入学"]["編入学年月日"] = $row["ENT_DATE"];
            if($this->schoolDiv != 0){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["編入学"]["編入学時学年"] = $row["ANNUAL"];
            }else{
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["編入学"]["編入学時学年"] = $row["GRADE"];
            }
            if($row["ENT_REASON"] != ""){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["編入学"]["備考"] = $row["ENT_REASON"];
            }
        }else{
            //入学
            $data["指導要録情報"]["学籍の記録"]["異動状況"]["入学"]["入学年月日"] = $row["ENT_DATE"];
            if($row["ENT_REASON"] != ""){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["入学"]["備考"] = $row["ENT_REASON"];
            }
        }
        
        if($row["GRD_DIV"] == "1"){
            //卒業
            $data["指導要録情報"]["学籍の記録"]["異動状況"]["卒業"]["卒業年月日"] = $row["GRD_DATE"];
        }else if($row["GRD_DIV"] == "2"){
            //退学
            $data["指導要録情報"]["学籍の記録"]["異動状況"]["退学"]["退学年月日"] = $row["GRD_DATE"];
            if($row["GRD_REASON"] != ""){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["退学"]["備考"] = $row["GRD_REASON"];
            }
        }else if($row["GRD_DIV"] == "3"){
            //転学
            $data["指導要録情報"]["学籍の記録"]["異動状況"]["転学"]["転学年月日"] = $row["BASE_REMARK1"];
            if($row["GRD_DATE"] != ""){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["転学"]["学校を去った日"] = $row["GRD_DATE"];
            }
            if($row["GRD_REASON"] != ""){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["転学"]["備考"] = $row["GRD_REASON"];
            }
        }
        
        //学籍の記録＞異動状況＞留学等
        $query = knjo102Query::getAbroad($this->SCHREGNO, "1");
        $cnt = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        if($cnt > 0){
            $query = knjo102Query::getAbroad($this->SCHREGNO);
            $result = $db->query($query);
            
            $count = 0;
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["留学等"][$count]["留学開始年月日"] = $row["TRANSFER_SDATE"];
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["留学等"][$count]["留学終了年月日"] = $row["TRANSFER_EDATE"];
                if($row["TRANSFERREASON"] != ""){
                    $data["指導要録情報"]["学籍の記録"]["異動状況"]["留学等"][$count]["備考"] = $row["TRANSFERREASON"];
                }
                $count++;
            }
        }
        
        //学籍の記録＞異動状況＞進学先就職先等
        $query = knjo102Query::getGrdAftCnt($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        if($row["COUNT"] != "" && $row["SENKOU_KIND"] == "0"){
            //進学先
            $query = knjo102Query::getGrdAftSch($this->SCHREGNO);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if(mb_substr($row["STAT_CD"], 0, 7) != "9999999"){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["進学先就職先等"] = $row["SCHOOL_NAME"]." ".$row["FACULTYNAME"]." ".$row["DEPARTMENTNAME"];
            }else{
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["進学先就職先等"] = $row["THINKEXAM"];
            }
            
        }else if($row["COUNT"] != "" && $row["SENKOU_KIND"] == "1"){
            //就職先
            $query = knjo102Query::getGrdAftComp($this->SCHREGNO);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if(mb_substr($row["STAT_CD"], 0, 7) != "9999999"){
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["進学先就職先等"] = $row["COMPANY_NAME"];
            }else{
                $data["指導要録情報"]["学籍の記録"]["異動状況"]["進学先就職先等"] = $row["JOB_THINK"];
            }

        }
        
        //学籍の記録＞課程名・学科名
        $query = knjo102Query::getCourseMajor($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        $data["指導要録情報"]["学籍の記録"]["課程名"] = $row["COURSE"];
        $data["指導要録情報"]["学籍の記録"]["学科名"] = $row["MAJOR"];
        
        
        //指導要録情報＞各教科_科目等の修得単位数の記録
        if($this->SYUBETU != 2){    //知的障害者のときは使用しない
            //各教科_科目等の修得単位数の記録＞各教科に共通する各教科_科目＞教科_科目
            $query = knjo102Query::getCredit($this->SCHREGNO, "", "1");   //学籍、共通科目、カウント
            $cnt = $db->getOne($query);
            
            if($cnt > 0){
                $query = knjo102Query::getCredit($this->SCHREGNO, "", "");
                $result = $db->query($query);
                
                $count = 0;
                
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    //学校設定教科・科目を使うかどうかの判断フラグ
                    $originalKyoukaFlg = 0;
                    $originalKamokuFlg = 0;
                    
                    if($row["CLASS"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$count]["教科"] = $row["CLASS"];
                    }else{
                        $originalKyoukaFlg = 1;
                    }
                    if($row["SUBCLASS"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$count]["科目"]["科目名"] = $row["SUBCLASS"];
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$count]["科目"]["修得単位数"] = $row["SUM_CREDIT"];
                    }else{
                        $originalKamokuFlg = 1;
                    }
                    
                    if($originalKyoukaFlg != 0){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$count]["学校設定教科"] = $row["CLASSNAME"];
                    }
                    if($originalKamokuFlg != 0){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$count]["学校設定科目"]["学校設定科目名"] = $row["SUBCLASSNAME"];
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["各学科に共通する各教科_科目"][$count]["学校設定科目"]["修得単位数"] = $row["SUM_CREDIT"];
                    }
                    $count++;
                }
            }
            
            
            //各教科_科目等の修得単位数の記録＞主として専門学科において開設される各教科_科目＞教科_科目
            $query = knjo102Query::getCredit($this->SCHREGNO, "1", "1");   //学籍、専門科目、カウント
            $cnt = $db->getOne($query);
            
            if($cnt > 0){
                $query = knjo102Query::getCredit($this->SCHREGNO, "1", "");
                $result = $db->query($query);
                
                $count = 0;
                
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    //学校設定教科・科目を使うかどうかの判断フラグ
                    $originalKyoukaFlg = 0;
                    $originalKamokuFlg = 0;
                    
                    if($row["CLASS"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$count]["教科"] = $row["CLASS"];
                    }else{
                        $originalKyoukaFlg = 1;
                    }
                    if($row["SUBCLASS"] != ""){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$count]["科目"]["科目名"] = $row["SUBCLASS"];
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$count]["科目"]["修得単位数"] = $row["SUM_CREDIT"];
                    }else{
                        $originalKamokuFlg = 1;
                    }
                    
                    if($originalKyoukaFlg != 0){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$count]["学校設定教科"] = $row["CLASSNAME"];
                    }
                    if($originalKamokuFlg != 0){
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$count]["学校設定科目"]["学校設定科目名"] = $row["SUBCLASSNAME"];
                        $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["主として専門学科において開設される各教科_科目"][$count]["学校設定科目"]["修得単位数"] = $row["SUM_CREDIT"];
                    }
                    
                    $count++;
                }
            }
            
            //各教科_科目等の修得単位数の記録＞留学
            $query = knjo102Query::getAbroad($this->SCHREGNO, "1");
            $cnt = $db->getOne($query);
            
            if($cnt > 0){
                $query = knjo102Query::getAbroadCredit($this->SCHREGNO);
                $credit = $db->getOne($query);
                
                $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["留学"]["修得単位数"] = $credit;
            }
            
            //各教科_科目等の修得単位数の記録＞自立活動($this->SYUBETU = 1のときのみ)
            if($this->SYUBETU != 0){
                /*$query = knjo102Query::getZiritsu($this->SCHREGNO, "1");
                $cnt = $db->getOne($query);
                
                if($cnt > 0){
                    $query = knjo102Query::getZiritsu($this->SCHREGNO);
                    $credit = $db->getOne($query);
                    
                    $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["自立活動"]["修得単位数"] = $credit;
                }*/
            }
            
            //各教科_科目等の修得単位数の記録＞総合的な学習の時間
            $query = knjo102Query::getIntegrated($this->SCHREGNO, "1");
            $cnt = $db->getOne($query);
            
            if($cnt > 0){
                $query = knjo102Query::getIntegrated($this->SCHREGNO);
                $creRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["総合的な学習の時間"]["総合的な学習の名称"] = $creRow["CLASSNAME"];
                $data["指導要録情報"]["各教科_科目等の修得単位数の記録"]["総合的な学習の時間"]["修得単位数"] = $creRow["CREDIT"];
            }
        }
        if($this->SYUBETU != 2){
            //指導要録情報＞指導に関する記録1($this->SYUBETU != 2のとき(知的障害者以外のとき)のみ)
            //指導に関する記録1を繰り返すようにする
            
            //指導に関する記録1＞学年
            $query = knjo102Query::getShidouGrade($this->SCHREGNO);
            $result = $db->query($query);
            
            $count = 0;
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //指導に関する記録1＞様式種別
                if($this->SYUBETU != 1){
                    $data["指導要録情報"]["指導に関する記録1"][$count]["様式種別"] = "01";
                }else{
                    $data["指導要録情報"]["指導に関する記録1"][$count]["様式種別"] = "02";
                }
            
                if($this->schoolDiv != 1){
                    $data["指導要録情報"]["指導に関する記録1"][$count]["学年"] = $row["GRADE"];
                }else{
                    $data["指導要録情報"]["指導に関する記録1"][$count]["学年"] = $row["ANNUAL"];
                }
                
                //指導に関する記録1＞原級留置回数
                $query = knjo102Query::getStayCntTotal($this->SCHREGNO, $row["ANNUAL"]);
                $stayRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                if($stayRow["COUNT"] > 1){
                    $data["指導要録情報"]["指導に関する記録1"][$count]["原級留置回数"] = $stayRow["COUNT"];
                }
                
                //指導に関する記録1＞各学科に共通する各教科_科目
                $query = knjo102Query::getCreditTotal($this->SCHREGNO, $row["ANNUAL"], "", "1");   //学籍、共通科目、カウント
                $cnt = $db->getOne($query);
                
                if($cnt > 0){
                    $query = knjo102Query::getCreditTotal($this->SCHREGNO, $row["ANNUAL"], "");   //学籍、共通科目
                    $kresult = $db->query($query);
                    
                    $kcount = 0;
                    
                    while($krow = $kresult->fetchRow(DB_FETCHMODE_ASSOC)){
                        //学校設定教科・科目を使うかどうかの判断フラグ
                        $originalKyoukaFlg = 0;
                        $originalKamokuFlg = 0;
                        
                        if($krow["CLASS"] != "" && $krow["SPECIALDIV"] != "2"){
                            $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["教科"] = $krow["CLASS"];
                        }else{
                            $originalKyoukaFlg = 1;
                        }
                        if($krow["SUBCLASS"] != "" && $krow["SPECIALDIV"] != "2"){
                            $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["科目"]["科目名"] = $krow["SUBCLASS"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["科目"]["評定"] = $krow["VALUATION"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["科目"]["修得単位数"] = $krow["GET_CREDIT"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["科目"]["修得単位数の計"] = $krow["TOTAL"];
                            if($krow["REMARK"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["科目"]["備考"] = $krow["REMARK"];
                            }else if($krow["GET_CREDIT"] == "0" || $krow["GET_CREDIT"] == ""){
                                $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["科目"]["備考"] = "履修単位数(".$krow["COMP_CREDIT"].")";
                            }
                        }else{
                            $originalKamokuFlg = 1;
                        }
                        
                        if($originalKyoukaFlg != 0){
                            $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["学校設定教科"] = $krow["CLASSNAME"];
                        }
                        if($originalKamokuFlg != 0){
                            $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["学校設定科目"]["学校設定科目名"] = $krow["SUBCLASSNAME"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["学校設定科目"]["学校設定科目評定"] = $krow["VALUATION"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["学校設定科目"]["学校設定科目修得単位数"] = $krow["GET_CREDIT"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["学校設定科目"]["学校設定科目修得単位数の計"] = $krow["TOTAL"];
                            if($krow["REMARK"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["学校設定科目"]["学校設定科目備考"] = $krow["REMARK"];
                            }else if($krow["GET_CREDIT"] == "0" || $krow["GET_CREDIT"] == ""){
                                $data["指導要録情報"]["指導に関する記録1"][$count]["各学科に共通する各教科_科目"][$kcount]["学校設定科目"]["学校設定科目備考"] = "履修単位数(".$krow["COMP_CREDIT"].")";
                            }
                        }
                        
                        $kcount++;
                    }
                }
                
                //指導に関する記録1＞主として専門学科において開設される各教科_科目
                $query = knjo102Query::getCreditTotal($this->SCHREGNO, $row["ANNUAL"], "1", "1");   //学籍、専門科目、カウント
                $cnt = $db->getOne($query);
                
                if($cnt > 0){
                    $query = knjo102Query::getCreditTotal($this->SCHREGNO, $row["ANNUAL"], "1");   //学籍、共通科目
                    $kresult = $db->query($query);
                    
                    $kcount = 0;
                    
                    while($krow = $kresult->fetchRow(DB_FETCHMODE_ASSOC)){
                        //学校設定教科・科目を使うかどうかの判断フラグ
                        $originalKyoukaFlg = 0;
                        $originalKamokuFlg = 0;
                        
                        if($krow["CLASS"] != "" && $krow["SPECIALDIV"] != "2"){
                            $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["教科"] = $krow["CLASS"];
                        }else{
                            $originalKyoukaFlg = 1;
                        }
                        if($krow["SUBCLASS"] != "" && $krow["SPECIALDIV"] != "2"){
                            $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["科目"]["科目名"] = $krow["SUBCLASS"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["科目"]["評定"] = $krow["VALUATION"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["科目"]["修得単位数"] = $krow["GET_CREDIT"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["科目"]["修得単位数の計"] = $krow["TOTAL"];
                            if($krow["REMARK"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["科目"]["備考"] = $krow["REMARK"];
                            }else if($krow["GET_CREDIT"] == "0" || $krow["GET_CREDIT"] == ""){
                                $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["科目"]["備考"] = "履修単位数(".$krow["COMP_CREDIT"].")";
                            }
                        }else{
                            $originalKamokuFlg = 1;
                        }
                        
                        if($originalKyoukaFlg != 0){
                            $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["学校設定教科"] = $krow["CLASSNAME"];
                        }
                        if($origonalKamokuFlg != 0){
                            $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["学校設定科目"]["学校設定科目名"] = $krow["SUBCLASSNAME"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["学校設定科目"]["学校設定科目評定"] = $krow["VALUATION"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["学校設定科目"]["学校設定科目修得単位数"] = $krow["GET_CREDIT"];
                            $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["学校設定科目"]["学校設定科目修得単位数の計"] = $krow["TOTAL"];
                            if($krow["REMARK"] != ""){
                                $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["学校設定科目"]["学校設定科目備考"] = $krow["REMARK"];
                            }else if($krow["GET_CREDIT"] == "0" || $krow["GET_CREDIT"] == ""){
                                $data["指導要録情報"]["指導に関する記録1"][$count]["主として専門学科において開設される各教科_科目"][$kcount]["学校設定科目"]["学校設定科目備考"] = "履修単位数(".$krow["COMP_CREDIT"].")";
                            }
                        }

                        $kcount++;
                    }
                }
                
                //指導に関する記録1＞留学
                $query = knjo102Query::getAbroadCnt($this->SCHREGNO, "1");
                $abroadCnt = $db->getOne($query);
                
                if($abroadCnt > 0){
                    //留学の開始年月日と修得単位数を取得
                    $query = knjo102Query::getAbroadCnt($this->SCHREGNO);
                    $abResult = $db->query($query);
                    $abroadCredit = 0;
                    while($abRow = $abResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $date = str_replace("-", "/", $abRow["TRANSFER_SDATE"]);
                        $year = common::DateConv1($date, 12);
                        //年度でANNUALを取得しに行く
                        $query = knjo102Query::getAnnalYear($this->SCHREGNO, $year);
                        $getAnnual = $db->getOne($query);
                        if($getAnnual == $row["ANNUAL"] && $abRow["ABROAD_CREDITS"] != ""){
                            $abroadCredit = $abroadCredit + $abRow["ABROAD_CREDITS"];
                        }
                    }
                    if($abroadCredit > 0){
                        $data["指導要録情報"]["指導に関する記録1"][$count]["留学"]["修得単位数"] = $abroadCredit;
                    }
                }
                
                //指導に関する記録1＞総合的な学習の時間の記録
                $query = knjo102Query::getIntegratedCredit($this->SCHREGNO, $row["ANNUAL"], "1");   //カウント
                $cnt = $db->getOne($query);
                
                if($cnt > 0){
                    $query = knjo102Query::getIntegratedCredit($this->SCHREGNO, $row["ANNUAL"]);
                    $sresult = $db->query($query);
                    
                    $icount = 0;
                    
                    while($srow = $sresult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $data["指導要録情報"]["指導に関する記録1"][$count]["総合的な学習の時間の記録"]["修得単位数"] = $srow["SUM_CREDIT"];
                        if($srow["TOTALSTUDYACT"] != ""){
                            $data["指導要録情報"]["指導に関する記録1"][$count]["総合的な学習の時間の記録"]["学習活動"] = $srow["TOTALSTUDYACT"];
                        }
                        if($srow["TOTALSTUDYVAL"] != ""){
                            $data["指導要録情報"]["指導に関する記録1"][$count]["総合的な学習の時間の記録"]["評価"] = $srow["TOTALSTUDYVAL"];
                        }
                        
                        $icount++;
                    }
                }
                
                //指導に関する記録1＞特別活動の記録
                $query = knjo102Query::getSpecialAct($this->SCHREGNO, $row["ANNUAL"], "1");
                $cnt = $db->getOne($query);
                
                if($cnt > 0){
                    $query = knjo102Query::getSpecialAct($this->SCHREGNO, $row["ANNUAL"]);
                    $sresult = $db->query($query);
                    
                    $act = "";
                    $cnm = "";
                    while($srow = $sresult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $act .= $cnm.$srow["SPECIALACTREMARK"];
                        $cnm = "\n\n";
                    }
                    
                    $data["指導要録情報"]["指導に関する記録1"][$count]["特別活動の記録"]["活動の状況"] = $act;
                }
                
                if($this->SYUBETU != 0){
                    //指導に関する記録1＞自立活動の記録($this->SYUBETU = 1(身体障害者)のときのみ)
                    /*$query = knjo102Query::getZirituAct($this->SCHREGNO, "1");    //対象テーブルなし
                    $cnt = $db->getOne($query);
                    
                    if($cnt > 0){
                        $query = knjo102Query::getZirituAct($this->SCHREGNO);
                        $act = $db->getOne($query);
                        
                        $data["指導要録情報"]["指導に関する記録1"][$count]["自立活動の記録"]["自立の状況"] = $act;
                    }*/
                }
                
                //指導に関する記録1＞総合所見及び指導上参考となる諸事項
                $query = knjo102Query::getTotalSyoken($this->SCHREGNO, $row["ANNUAL"], "1");
                $cnt = $db->getOne($query);
                
                if($cnt > 0){
                    $query = knjo102Query::getTotalSyoken($this->SCHREGNO, $row["ANNUAL"]);
                    $sresult = $db->query($query);
                    
                    $remark = "";
                    $cnm = "";
                    
                    while($srow = $sresult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $remark .= $cnm.$srow["TOTALREMARK"];
                        $cnm = "\n\n";
                    }
                    $data["指導要録情報"]["指導に関する記録1"][$count]["総合所見及び指導上参考となる諸事項"]["事項_事実_所見"] = $remark;
                }
                
                //指導に関する記録1＞出欠の記録
                $query = knjo102Query::getAttendRec($this->SCHREGNO, $row["ANNUAL"]);
                $aresult = $db->query($query);
                
                $classCnt = 0;
                $stopCnt = 0;
                $abroadCnt = 0;
                $mustCnt = 0;
                $absenceCnt = 0;
                $presentCnt = 0;
                
                $bikou = "";
                $cnm = "";
                
                while($arow = $aresult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $classCnt = $classCnt + $arow["CLASSDAYS"];
                    
                    $stopCnt = $stopCnt + $arow["SUSPEND"] + $arow["MOURNING"];
                    
                    $abroadCnt = $abroadCnt + $arow["ABROAD"];
                    
                    $must = $arow["CLASSDAYS"] - ($arow["SUSPEND"] + $arow["MOURNING"]);
                    $mustCnt = $mustCnt + $must;
                    
                    $absenceCnt = $absenceCnt + ($arow["ACCIDENTNOTICE"] + $arow["NOACCIDENTNOTICE"]);
                    
                    $presentCnt = $presentCnt + $arow["PRESENT"];
                    
                    if($arow["ATTENDREC_REMARK"] != ""){
                        $bikou .= $cnm.$arow["ATTENDREC_REMARK"];
                        $cnm = "\n";
                    }

                }
                
                //備考を取得する
                $query = knjo102Query::getAttendRemark($this->SCHREGNO, $row["ANNUAL"], "1");
                $remarkCnt = $db->getOne($query);
                $bikou = "";
                $cnm = "";
                if($remarkCnt > 0){
                    $query = knjo102Query::getAttendRemark($this->SCHREGNO, $row["ANNUAL"]);
                    $rResult = $db->query($query);
                    while($rRow = $rResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $bikou .= $cnm.$rRow["ATTENDREC_REMARK"];
                        $cnm = "\n";
                    }
                }
                
                $data["指導要録情報"]["指導に関する記録1"][$count]["出欠の記録"]["授業日数"] = $classCnt;
                $data["指導要録情報"]["指導に関する記録1"][$count]["出欠の記録"]["出席停止_忌引等の日数"] = $stopCnt;
                $data["指導要録情報"]["指導に関する記録1"][$count]["出欠の記録"]["留学中の授業日数"] = $abroadCnt;
                $data["指導要録情報"]["指導に関する記録1"][$count]["出欠の記録"]["出席しなければならない日数"] = $mustCnt;
                $data["指導要録情報"]["指導に関する記録1"][$count]["出欠の記録"]["欠席日数"] = $absenceCnt;
                $data["指導要録情報"]["指導に関する記録1"][$count]["出欠の記録"]["出席日数"] = $presentCnt;
                
                $data["指導要録情報"]["指導に関する記録1"][$count]["出欠の記録"]["備考"] = $bikou;
                
                $count++;
            }
        }else{
            //指導要録情報＞指導に関する記録2($this->SYUBETU = 2のときのみ)
            $query = knjo102Query::getShidouGrade($this->SCHREGNO);
            $result = $db->query($query);
            
            $count = 0;
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){    //学年ごとにデータ取得にいく
                
                //指導に関する記録2＞様式種別
                $data["指導要録情報"]["指導に関する記録2"][$count]["様式種別"] = "03";
                
                //指導に関する記録2＞学年
                if($this->schoolDiv != 0){
                    $data["指導要録情報"]["指導に関する記録2"][$count]["学年"] = $row["ANNUAL"];
                }else{
                    $data["指導要録情報"]["指導に関する記録2"][$count]["学年"] = $row["GRADE"];
                }
                
                //指導に関する記録2＞原級留置回数
                $squery = knjo102Query::getStayCntShidou2($this->SCHREGNO, $row["ANNUAL"]);
                $scnt = $db->getOne($squery);
                
                if($scnt != ""){
                    $data["指導要録情報"]["指導に関する記録2"][$count]["原級留置回数"] = $scnt;
                }
                
                //指導に関する記録2＞各教科_特別活動_自立活動の記録
                $squery = knjo102Query::getShidou2Record($this->SCHREGNO, $row["ANNUAL"], "1");
                $scnt = $db->getOne($squery);
                
                if($scnt > 0){
                    $squery = knjo102Query::getShidou2Record($this->SCHREGNO, $row["ANNUAL"]);
                    $sresult = $db->query($squery);
                    
                    $record = "";
                    $cnm = "";
                    $time = "";
                    
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                        $record .= $cnm.$row["REMARK1"];
                        $time = $row["SUM"];
                        
                        $cnm = "\n";
                    }
                    
                    $data["指導要録情報"]["指導に関する記録2"][$count]["各教科_特別活動_自立活動の記録"]["記録"] = $record;
                    $data["指導要録情報"]["指導に関する記録2"][$count]["各教科_特別活動_自立活動の記録"]["総授業時数"] = $time;
                    
                }
                
                //指導に関する記録2＞総合的な学習の時間の記録・総合所見及び指導上参考となる諸事項
                $squery = knjo102Query::getShidou2Integrated($this->SCHREGNO, $row["ANNUAL"], "1");
                $scnt = $db->getOne($squery);
                
                if($scnt > 0){
                    $squery = knjo102Query::getShidou2Integrated($this->SCHREGNO, $row["ANNUAL"]);
                    $srow = $db->getRow($squery, DB_FETCHMODE_ASSOC);
                    
                    $data["指導要録情報"]["指導に関する記録2"][$count]["総合的な学習の時間の記録"]["学習活動"] = $row["TOTALSTUDYACT"];
                    $data["指導要録情報"]["指導に関する記録2"][$count]["総合的な学習の時間の記録"]["評価"] = $row["TOTALSTUDYVAL"];

                    $data["指導要録情報"]["指導に関する記録2"][$count]["総合所見及び指導上参考となる諸事項"]["事項_事実_所見"] = $row["TOTALREMARK"];
                }
                
                //指導に関する記録2＞出欠の記録
                $squery = knjo102Query::getAttendRec($this->SCHREGNO, $row["ANNUAL"]);
                $sresult = $db->query($squery);
                
                
                $classCnt = 0;
                $stopCnt = 0;
                $abroadCnt = 0;
                $mustCnt = 0;
                $absenceCnt = 0;
                $presentCnt = 0;
                
                while($srow = $sresult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $classCnt = $classCnt + $srow["CLASSDAYS"];
                    
                    $stopCnt = $stopCnt + $srow["SUSPEND"] + $srow["MOURNING"];
                    
                    $abroadCnt = $abroadCnt + $srow["ABROAD"];
                    
                    $must = $srow["CLASSDAYS"] - ($srow["SUSPEND"] + $srow["MOURNING"]);
                    $mustCnt = $mustCnt + $must;
                    
                    $absenceCnt = $absenceCnt + ($srow["ACCIDENTNOTICE"] + $srow["NOACCIDENTNOTICE"]);
                    
                    $presentCnt = $presentCnt + $srow["PRESENT"];
                    
                }
                
                //備考を取得する
                $query = knjo102Query::getAttendRemark($this->SCHREGNO, $row["ANNUAL"], "1");
                $remarkCnt = $db->getOne($query);
                $bikou = "";
                $cnm = "";
                if($remarkCnt > 0){
                    $query = knjo102Query::getAttendRemark($this->SCHREGNO, $row["ANNUAL"]);
                    $rResult = $db->query($query);
                    while($rRow = $rResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $bikou .= $cnm.$rRow["ATTENDREC_REMARK"];
                        $cnm = "\n";
                    }
                }

                $data["指導要録情報"]["指導に関する記録2"][$count]["出欠の記録"]["授業日数"] = $classCnt;
                $data["指導要録情報"]["指導に関する記録2"][$count]["出欠の記録"]["出席停止_忌引等の日数"] = $stopCnt;
                $data["指導要録情報"]["指導に関する記録2"][$count]["出欠の記録"]["留学中の授業日数"] = $abroadCnt;
                $data["指導要録情報"]["指導に関する記録2"][$count]["出欠の記録"]["出席しなければならない日数"] = $mustCnt;
                $data["指導要録情報"]["指導に関する記録2"][$count]["出欠の記録"]["欠席日数"] = $absenceCnt;
                $data["指導要録情報"]["指導に関する記録2"][$count]["出欠の記録"]["出席日数"] = $presentCnt;
                
                $data["指導要録情報"]["指導に関する記録2"][$count]["出欠の記録"]["備考"] = $bikou;

                
                $count++;
            }
            
            //指導要録情報＞入学時の障害の状況($this->SYUBETU = 2のときのみ)
            /*$query = knjo102Query::getEnt($this->SCHREGNO, "1");
            $cnt = $db->getOne($query);
            
            if($cnt > 0){
                $query = knjo102Query::getEnt($this->SCHREGNO);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                $data["指導要録情報"]["入学時の障害の状況"] = $row[""];
            }*/
        }
        
        
        
        
        
        //送信日時
        $data["送信日時"] = date("c");


        //DB切断
        Query::dbCheckIn($db);



        //初期値
        $count = 0;
        $past = "message";
        
        //functionに送る
        knjo102Model::create($data, $count, $past, $this);

        
        /*new dBug($this->new);
        exit;*/

        //タイトル部分 外から渡してこれるように
        $elementName = "指導要録情報メッセージ";
        $attribute = array("xmlns" => "urn:applic:xmlns:pf:edu:schema:2016-01",
                           "xmlns:xsi" => "http://www.w3.org/2001/XMLSchema-instance",
                           "xmlns:lgxml" => "urn:applic:xmlns:pf:lgxml:schema:2016-01",
                           "xsi:schemaLocation" => "urn:applic:xmlns:pf:edu:schema:2016-01 eduAK3101s-2016-01.xsd");


        //出力
        $today = date("Ymd");
        $time = date("His");
        $fileName = $this->schoolName."_".$this->SCHREGNO."_S".$today.$time.'.xml';  //  ファイル名
         
        $this->dom = new DomDocument('1.0');  // DOMを作成
        $this->dom->encoding = "UTF-8"; // 文字コードをUTF-8に
        $this->dom->formatOutput = true; // 出力XMLを整形(改行,タブ)する
        
        /*$this->message = $this->dom->appendChild($this->dom->createElement('指導要録情報メッセージ'));
        //$this->message = $this->dom->appendChild($this->dom->createElement('生徒健康診断票情報メッセージ'));
        $this->message->setAttribute("xmlns", "urn:applic:xmlns:pf:edu:schema:2016-01");
        $this->message->setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        $this->message->setAttribute("xsi:schemaLocation", "urn:applic:xmlns:pf:edu:schema:2016-01 eduAK3101s-2016-01.xsd");
        //$this->message->setAttribute("xsi:schemaLocation", "urn:applic:xmlns:pf:edu:schema:2016-01 eduAK3201s-2016-01.xsd");*/
        
        $this->message = $this->dom->appendChild($this->dom->createElement($elementName));
        foreach($attribute as $key => $val){
            $this->message->setAttribute($key, $val);
        }
        

        for($count = 1; $count<count($this->new)+1; $count++){
            knjo102Model::makeXML($this->new[$count], $this); 
        }
        
        
        // HTTPヘッダを出力して
        header("Content-disposition: attachment; filename=".mb_convert_encoding($fileName, 'SJIS-win', 'UTF-8'));
        header("Content-type: application/octet-stream; name=" . $fileName);
         
        // DomXMLをXML形式で出力
        echo $this->dom->saveXML();

        $flg = true;
        //Query::dbCheckIn($db);

        if ($flg) {
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
        
    }


    //外から呼ぶためのfunction
    function createXML($data, $elementName, $attribute, $fileName)
    {
        $flg = false;


        //初期値
        $count = 0;
        $past = "message";
        
        //functionに送る
        knjo102Model::create($data, $count, $past, $this);

        
         
        $this->dom = new DomDocument('1.0');  // DOMを作成
        $this->dom->encoding = "UTF-8"; // 文字コードをUTF-8に
        $this->dom->formatOutput = true; // 出力XMLを整形(改行,タブ)する
        
        
        $this->message = $this->dom->appendChild($this->dom->createElement($elementName));
        foreach($attribute as $key => $val){
            $this->message->setAttribute($key, $val);
        }
        

        for($count = 1; $count<count($this->new)+1; $count++){
            knjo102Model::makeXML($this->new[$count], $this); 
        }
        
        
        // HTTPヘッダを出力して
        header("Content-disposition: attachment; filename=".mb_convert_encoding($fileName, 'SJIS-win', 'UTF-8'));
        header("Content-type: application/octet-stream; name=" . $fileName);
         
        // DomXMLをXML形式で出力
        echo $this->dom->saveXML();

        $flg = true;
        //Query::dbCheckIn($db);

        if ($flg) {
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
        
    }





    function makeXML($array, &$make) {
        foreach($array as $key => $val){
            if(is_array($val)){
                foreach($val as $key2 => $val2){
                    $make->{$key2} = $make->{$key}->appendChild($make->dom->createElement("{$val2}"));
                }
            }else{
                $make->{$key}->appendChild($make->dom->createTextNode($array[$key]));
            }
        }
        
    }

    function create(&$array, $count, $past, &$data){
        $count++;
        
        foreach($array as $key => $val){
            $i = count($data->new[$count]);
            
            if(is_numeric($key)){
                
                if($key != "0"){
                    //ひとつ上の親になる部分の配列増やしたい
                    //$pastの後ろの数字が何文字かカウントする
                    $numCnt = 0;
                    for($j = 1; $j<mb_strlen($past); $j++){
                        $a = -1*$j;
                        $back = mb_substr($past, $a, 1);
                        if(is_numeric($back)){
                            $numCnt++;
                        }else{
                            break;
                        }
                    }
                    
                    
                    //$pastの文字列を数字で区切る
                    $parent = preg_split("/[0-9]/", $past);
                    
                    $parent = array_filter($parent);
                    $parent = array_values($parent);
                    
                    //後ろから2番目の配列の中身の文字数を数える
                    $mojicnt = mb_strlen($parent[count($parent)-1]);
                    
                    //数えた文字数+1文字分後ろから削除した文字列を作成する
                    $parname = mb_substr($past, 0, mb_strlen($past)-$mojicnt-$numCnt);
                    
                    //配列のキーに数字を追加して被らないようなキーにする
                    $now = $past.$key;
                    
                    //親になるキーを作る
                    $data->new[$count-1][$parname][$now] = $data->new[$count-1][$parname][$past];
                    
                }else{
                    $now = $past;
                }
                
                //$count--;
            }else{
                $now = $past.$key.$i;
                $data->new[$count][$past][$now] = $key;
                
            }
            
            if(is_array($val)){
                knjo102Model::create($val, $count, $now, $data);
            }else{
                $count++;
                $i = count($data->new[$count]);
                
                $data->new[$count][$now] = $val;
                
                $count--;
            }
        }
        return;
    }


    function getProperties()
    {
        $this->properties = array();
        //初期値
        $this->properties["useSchregRegdHdat"]  = "";

        $arr_useUnAdminMenuPrgid = array();
        $arr_useSubMenuId = array();
        $retVal = "";
       
        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/prgInfo.properties")) {
            $filename = CONFDIR ."/prgInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/prgInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        while ($line = fgets($fp,1024)) {
            foreach ($this->properties as $key => $value) {
                $pos = strpos($line, $key);
                if ($pos === false) {
                } else {
                    $retVal = str_replace($key." = ", "", $line);
                    $this->properties[$key] = str_replace("\r\n", "", $retVal);
                    if ($key == "useUnAdminMenuPrgid") {
                        $arr_useUnAdminMenuPrgid[] = str_replace("\r\n", "", $retVal);
                    }
                    if ($key == "useSubMenuId") {
                        $arr_useSubMenuId[] = str_replace("\r\n", "", $retVal);
                    }
                }
            }
        }
        fclose($fp);

        return $retVal;
    }
    


}

?>
