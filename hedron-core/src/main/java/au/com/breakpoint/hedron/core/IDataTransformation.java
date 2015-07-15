//                       __________________________________
//                ______|         Copyright 2008           |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.core;

/**
 * Interface that performs the transformation phase of the 3 part process
 * (poll:IDataTransformationQueue, transform:IDataTransformation,
 * persist:IDataPersistence).
 *
 * @param <TArg>
 *            type of the data to be transformed
 * @param <TReturn>
 *            type of the data to be transformed into
 */
@FunctionalInterface
public interface IDataTransformation<TArg, TReturn>
{
    /**
     * Transform one arg to a new value.
     *
     * @return The result of the transformation.
     */
    TReturn getValue (final TArg a);
}
