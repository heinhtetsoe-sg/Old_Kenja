# kanji=漢字
# $Id: readme.txt 56590 2017-10-22 13:01:54Z maeshiro $

2012/11/30  1.KNJM390を元に新規作成

2012/12/10  1.「スクーリング種別：その他」のとき、校時の選択を有効にするように変更した。
            2.登録生徒一覧に単位時間を追加した。

2012/12/12  1.時間割がない場合は、エラーとする。
            2.教科CD < 91 が対象に変更
            2.登校の場合のみ、時間割がない場合は、エラーとする。

2013/01/15  1.バーコードから入力タイムアウト対応

2013/04/11  1.SCHOOLING_COMBINED_DATに設定している開講しない講座（ATTEND_SUBCLASSCDの講座）を講座コンボに表示しないよう修正
            2.SCHOOLING_COMBINED_DATに設定している開講講座（COMBINED_SUBCLASSCDの講座）を開講しない講座（ATTEND_SUBCLASSCDの講座）に出席登録できるよう修正

2013/04/12  1.テーブル名変更
                - SCHOOLING_COMBINED_DAT ⇒ SUBCLASS_SCHOOLING_COMBINED_DAT
            2.開講講座（COMBINED_SUBCLASSCDの講座）自身も出席登録できるよう修正
            3.データ2重登録、不要なデータチェックを修正
            
2013/04/18  1.エラーメッセージ未出力の部分を表示するよう修正

2013/05/17  1.学籍番号入力欄は、IMEモードオフ

2013/06/07  1.チェック処理追加
            -- 同じ日付、校時で登録しようとした時、エラーとする。（科目、講座に関係なく）「既にこの時間は登録されています。登録できません。」
            2.チェック処理追加
            -- 同じ講座、日付で登録しようとした時、エラーとする。（校時に関係なく）「同日2回目の登録です。登録できません。」

2013/06/10  1.チェック処理追加
            -- スクーリング種別その他で同じ日付、講座で登録しようとした時、メッセージ表示（校時に関係なく）「同日2回目の登録です。登録しますか？」
            -- 「OK」の場合、登録する。「キャンセル」の場合、登録しない。

2013/06/11  1.スクーリング種別が放送の場合、以下のチェックをしない
            -- 同じ日付、校時で登録しようとした時、エラーとする。（科目、講座に関係なく）「既にこの時間は登録されています。登録できません。」

2013/06/12  1.登校・その他登録の際に同じ日付、講座で放送が登録されていてもエラーとしないように修正

2014/04/21  1.SUBCLASS_SCHOOLING_COMBINED_DAT、CHAIR_STD_DATの指定条件追加（データが複数登録される不具合対応）

2016/08/15  1.警告エラー修正。PHP5.4からのWarning: Illegal string offset
            2.名称マスタ「M001」「4：登校スクーリング２」の追加に伴い修正
            -- 現在：
            -- 　　「1：登校スクーリング」時間割ありで作成済み
            -- 仕様追加後：
            -- 　　「4：登校スクーリング２」時間割なしの対応
            -- 　　【登録】の時、時間割（SCH_CHR_T_DAT）の存在チェックをしない。

2017/09/01  1.講座ソート処理修正
